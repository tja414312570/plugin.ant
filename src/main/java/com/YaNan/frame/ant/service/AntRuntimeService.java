package com.YaNan.frame.ant.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.AntContext;
import com.YaNan.frame.ant.AntContextConfigure;
import com.YaNan.frame.ant.abstracts.AbstractProcess;
import com.YaNan.frame.ant.annotations.Ant;
import com.YaNan.frame.ant.exception.AntClientHandlerOverFlow;
import com.YaNan.frame.ant.exception.AntInitException;
import com.YaNan.frame.ant.exception.AntRequestException;
import com.YaNan.frame.ant.exception.AntRuntimeException;
import com.YaNan.frame.ant.exception.ServiceNotRunningException;
import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.interfaces.AntDiscoveryService;
import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.model.AntProvider;
import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.ant.proxy.AntProxyMapper;
import com.YaNan.frame.ant.utils.MessageProcesser;
import com.YaNan.frame.ant.utils.MessageQueue;
import com.YaNan.frame.ant.utils.ObjectLock;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.utils.StringUtil;
import com.YaNan.frame.utils.asserts.Assert;
import com.YaNan.frame.utils.resource.PackageScanner;
import com.YaNan.frame.utils.resource.ResourceManager;

public class AntRuntimeService {
	private AntContext antContext;
	private boolean daemon = true;
	/**
	 * 客户端注册服务
	 */
	private AntClientRegisterService antClientRegisterService;
	/**
	 * 所有通道和连接处理器的映射
	 */
	private Map<SocketChannel,AntClientHandler> handlerMapping;
	/**
	 * 服务提供者集合
	 */
	private Map<String,AntClientHandler> serviceProviderMap ;
	/**
	 * select选择器服务
	 */
	private SelectorRunningService selectorRunningService;
	/**
	 * 任务处理线程
	 */
	private ExecutorService processExecutor;
	
	private static Logger logger = LoggerFactory.getLogger(AntRuntimeService.class);
	
	private volatile boolean available = false;
	
	private static final int MAX_ID_NUM = Integer.MAX_VALUE;
	
	private AtomicInteger idCount = new AtomicInteger();
	
	private AtomicInteger clientIdCount = new AtomicInteger();
	
	private Thread selectorThread;
	
	private AntRegisterService antRegisterServcie;
	
	private MessageQueue messageQueue;
	
	private final Object selectObjectLock = new Object();
	private AntDiscoveryService discoveryService;
	private int serverPort;
	public int getServerPort() {
		return serverPort;
	}
	public int getRID(){
		int rid = idCount.incrementAndGet();
		if(rid >= MAX_ID_NUM)
			idCount.set(0);
		return rid;
	}
	public AntRuntimeService(AntContext context) {
		antContext = context;
		this.antClientRegisterService = new AntClientRegisterService(this);
		this.messageQueue = new MessageQueue(this);
		this.antRegisterServcie = new AntRegisterService(this);
		handlerMapping = new ConcurrentHashMap<SocketChannel,AntClientHandler>();
		this.serviceProviderMap = new ConcurrentHashMap<String,AntClientHandler>();
		//如果没有设置处理线程数，设置默认线程数
		createProcessExector();
		selectorRunningService = new SelectorRunningService(this);
	}
	private void createProcessExector() {
		BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>(antContext.getContextConfigure().getTaskSize());
		if(antContext.getContextConfigure().getProcess()>0){
			if(antContext.getContextConfigure().getMaxProcess()<antContext.getContextConfigure().getProcess())
				antContext.getContextConfigure().setMaxProcess(antContext.getContextConfigure().getProcess()<<1);
			this.processExecutor = new ThreadPoolExecutor(antContext.getContextConfigure().getProcess(),
					antContext.getContextConfigure().getMaxProcess(), 
					antContext.getContextConfigure().getTimeout(), 
					TimeUnit.MILLISECONDS, 
					blockingQueue,
					new AntThreadFactory(antContext),
					new ThreadPoolExecutor.CallerRunsPolicy());
		}else {
			this.processExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
					Runtime.getRuntime().availableProcessors()<<1,
					antContext.getContextConfigure().getTimeout(),
					TimeUnit.MILLISECONDS,
					blockingQueue,
					new AntThreadFactory(antContext),
					new ThreadPoolExecutor.CallerRunsPolicy());
		}
	}
	
	public void registerHandler(SocketChannel socketChannel) throws ClosedChannelException {
		 socketChannel.register(selectorRunningService.getSelector(), SelectionKey.OP_CONNECT);
	}
	public void start()  {
		//访问注册中心
		logger.debug("start ant service ");
		logger.debug("try connect ant provider service");
		try {
			long t1 = System.currentTimeMillis();
			this.discoveryService = PlugsFactory.getPlugsInstance(AntDiscoveryService.class);
			//设置运行时
			discoveryService.setAntRuntimeService(this);
			//检查服务
			discoveryService.avaiable();
			//判断是否服务提供者
			if(StringUtil.isNotEmpty(antContext.getContextConfigure().getPort())) {
				startProvider();
				AntProvider antProvider = new AntProvider();
				antProvider.setHost(this.getContextConfigure().getHost());
				antProvider.setPort(this.serverPort);
				antProvider.setName(this.antContext.getContextConfigure().getName());
				logger.debug("try regist ant service "+antProvider);
				//注册服务
				discoveryService.registerService(antProvider);
				//开启服务提供者节点
			}//客户端则扫描
			AntProxyMapper proxy = PlugsFactory.getPlugsInstance(AntProxyMapper.class,this,ResourceManager.classPaths());
			proxy.execute();
			
			checkAndStartSelectorThread();
			logger.debug("ant service started at "+(System.currentTimeMillis()-t1)/1000+" s");
			//扫描所有调用的类，获取需要调用的服务的名称
			executeProcess(new AbstractProcess() {
				@Override
				public void execute() {
					logger.debug("start ant proxy scanner process");
					long t1 = System.currentTimeMillis();
					List<String> serviceList = scanAntService();
					//从服务中心下载服务并缓存到本地缓存
					serviceList.forEach(serviceName -> {
						try {
							if(!serviceName.equals(getContextConfigure().getName()))
								addServiceFromDiscoveryService(serviceName);
						}catch(Throwable t) {
							logger.error(t.getMessage(),t);
						}
						
					});
					logger.debug("ant proxy scanner process completed at "+(System.currentTimeMillis()-t1)/1000+" s");
				}
			});
			
		}catch (Throwable e) {
			throw new AntInitException("failed to start service",e);
		}
		
	}
	/**
	 * 尝试恢复服务
	 * @param handler
	 * @param t
	 * @return 
	 */
	public void tryRecoveryServiceAndNotifyDiscoveryService(AntClientHandler handler) {
		ObjectLock lock = null;
		Assert.isNull(handler, "handelr name is null!");
		AntProviderSummary antProviderSummary = handler.getAttribute(AntProviderSummary.class);
		if(antProviderSummary == null)
			return;
		String serviceName = antProviderSummary.getName();
		lock = ObjectLock.getLock(serviceName, this.getContextConfigure().getTimeout());
		if(lock.isLock())
			return;
		lock.tryLock();
		if(handlerMapping.containsKey(handler.getSocketChannel()))
			handlerMapping.remove(handler.getSocketChannel());
		else if(serviceProviderMap.containsKey(serviceName)) {
			lock.release();
			return;
		}
		if(serviceProviderMap.containsKey(serviceName))
			serviceProviderMap.remove(serviceName);
		int i = 0 ;
		lock.release();
		for(;;) {
			try {
				if(i++ > 10)
					break;
				AntClientHandler antClientHandler;
				//如果重新连接，重新发送未完成的数据
				if((antClientHandler = serviceProviderMap.get(serviceName)) != null) {
					//获取所有的未完成的请求
					List<AntMessagePrototype> list = messageQueue.getAllProcessingMessage(serviceName);
					//使用新的Handler继续发送消息
					list.forEach(message->antClientHandler.write(message));
					return;
				}
				lock = ObjectLock.getLock(serviceName, this.getContextConfigure().getTimeout());
				lock.tryLock();
				logger.error("try to recovery service "+antProviderSummary.getName()+" ["+i+"]");
				AntProviderSummary providerSummary = discoveryService.getService(serviceName);
				logger.debug("get service for "+serviceName+" info "+antProviderSummary);
				Assert.isNull(providerSummary,()->
					{
						try {
							Thread.sleep(this.getContextConfigure().getRetryInterval());
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				);
				clientService(providerSummary);
			} catch (Throwable e) {
				if(lock != null) {
					lock.release();
				}
			}
		}
		List<AntMessagePrototype> messageList = this.messageQueue.getAllProcessingMessage(serviceName);
		messageList.forEach(e->messageQueue.notifyException(e.getRID(), new AntRequestException("failed to invoke ant service!")));
		throw new AntRuntimeException("try to recovery service "+antProviderSummary.getName()+" failed,give up at ["+i+"]");
	}
	public void addServiceFromDiscoveryService(String serviceName) {
		ObjectLock lock = null;
		try {
			Assert.isNull(serviceName, "service name is null!");
			if(serviceProviderMap.containsKey(serviceName))
				return;
			lock = ObjectLock.getLock(serviceName, this.getContextConfigure().getTimeout());
			lock.tryLock();
			AntProviderSummary providerSummary = discoveryService.getService(serviceName);
			logger.debug("get service for "+serviceName+" info "+providerSummary);
			Assert.isNull(providerSummary,"could not found ant provider server :"+serviceName);
			clientService(providerSummary);
		} catch (Throwable e) {
			logger.error("add service ["+serviceName+"] failed!",e);
			if(lock != null) {
				lock.release();
			}
			throw new AntRuntimeException(e);
		}
	}
	private List<String> scanAntService() {
		List<String> result = new ArrayList<>();
		PackageScanner packageScanner = new PackageScanner();
		packageScanner.doScanner(cls -> {
			Ant ant = cls.getAnnotation(Ant.class);
			if(ant != null) {
				result.add(ant.value().trim());
			}
		});
		return result;
	}
	private void checkAndStartSelectorThread() {
		if(selectorThread == null || !selectorThread.isAlive()) {
			synchronized (selectObjectLock) {
				if(selectorThread == null || !selectorThread.isAlive()) {
					logger.debug("enable ant selector runtime service!");
					selectorThread = new Thread(selectorRunningService);
					selectorThread.setName("Ant Runtime Selector Thread");
//					selectorThread.setDaemon(daemon);
					selectorThread.start();
				}
			}
		}
	}
	public void execute(MessageProcesser messageProcesser) {
		if(antContext.getContextConfigure().getProcess() == 0){
			this.processExecutor.execute(messageProcesser);
		}else{
			messageProcesser.run();
		}
	}
	public void executeProcess(AbstractProcess process) {
		this.processExecutor.execute(process);
	}
	/**
	 * 向具体的客户端连接发送消息
	 * @param handler
	 * @param message
	 * @param lock
	 * @return
	 */
	public Object request(AntClientHandler handler,AntMessagePrototype message,boolean lock) {
		try {
			message.setClientHandler(handler);
			if(handler==null || ! handler.getSocketChannel().finishConnect())
				throw new ServiceNotRunningException(handler);
			message.setRID(getRID());
			messageQueue.addRequest(message);
//			logger.debug("request to '"+handler.getSocketChannel().getRemoteAddress()+"' Ant proxy："+message+"，lock："+lock);
			//写入数据
			if(lock)
				handler.writeAndLock(message);
			else
				handler.write(message);
			//如果调用有返回值
//			if(!message.getInvokeMethod().getReturnType().equals(void.class))
//							//给消息加锁
				return messageQueue.getResult(message);
//			return null;
		} catch (Throwable e) {
			throw new AntRequestException(e,handler);
		}
	}
	/**
	 * 请求Ant服务
	 * @param message
	 * @param lock
	 * @return
	 */
	public Object request(AntMessagePrototype message,boolean lock) {
		AntClientHandler handler = AntClientHandler.getHandler();
		if(handler == null)
			handler = serviceProviderMap.get(message.getService());
		return this.request(handler, message, lock);
	}
//	/**
//	 * 请求Ant Queen服务
//	 * @param message
//	 * @param lock
//	 * @return
//	 */
//	public Object requestQueen(AntMessagePrototype message,boolean lock) {
//		return this.request(this.queenClientHandler, message, lock);
//	}
	public AntClientRegisterService getAntClientRegisterService() {
		return antClientRegisterService;
	}
	public Map<String,AntClientHandler> getServiceProviderMap() {
		return serviceProviderMap;
	}
	public boolean isAvailable() {
		return available;
	}
	public void setAvailable(boolean available) {
		this.available = available;
	}
	/**
	 * 开启服务提供
	 */
	public void startProvider() {
		try {
			logger.debug("enable ant provider service");
			logger.debug("ant provider service name:"+antContext.getContextConfigure().getName());
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			String portStr = antContext.getContextConfigure().getPort();
			String[] ports = portStr.split("-");
			int port = Integer.valueOf(ports[0]);
			int maxPort = ports.length>1?Integer.valueOf(ports[1]):port;
			boolean failed = true;
			while(port <= maxPort) {
				try {
					serverSocketChannel.bind(
							new InetSocketAddress(
									Integer.valueOf(port)));
					failed = false;
					this.serverPort = port;
					break;
				}catch (IOException e) {
					logger.info("port ["+port+"] is occupied!",e);
					port++;
				}
			}
			if(failed)
				throw new IOException("port is occupied");
			logger.debug("listening server port:"+this.serverPort);
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(selectorRunningService.getSelector(), SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			throw new AntInitException(e);
		}
	}
	public boolean isDaemon() {
		return daemon;
	}
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}
	/**
	 * 获取链接 ID
	 * @return
	 */
	public int getClientId() {
		int rid = clientIdCount.incrementAndGet();
		if(rid >= MAX_ID_NUM)
			throw new AntClientHandlerOverFlow("the client handler over flow");
		return rid;
	}
	/**
	 * 连接到目标服务器
	 * @param providerSummary
	 * @param host
	 * @param port
	 * @throws IOException 
	 */
	public void clientService(AntProviderSummary providerSummary) throws IOException {
		logger.debug("connection to server "+providerSummary);
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.connect(new InetSocketAddress(providerSummary.getHost(),providerSummary.getPort()));
		socketChannel.register(selectorRunningService.getSelector(), SelectionKey.OP_CONNECT,providerSummary);
	}
	/**
	 * 获取一个连接服务
	 * @param name
	 * @return
	 */
	public AntClientHandler getClientHandler(String name) {
		AntClientHandler antClientHandler = this.serviceProviderMap.get(name);
		if(antClientHandler == null) {
			addServiceFromDiscoveryService(name);
			ObjectLock.getLock(name).tryLock();
		}
		antClientHandler = this.serviceProviderMap.get(name);
		if(antClientHandler == null) {
			throw new AntRuntimeException("could not found ant service for \""+name+"\"");	
		}
		AntClientHandler.setHandler(antClientHandler);
		return antClientHandler;
	}
	public AntClientHandler getHandler(SocketChannel socketChannel) {
		return handlerMapping.get(socketChannel);
	}
	public void addClientHandler(String name, AntClientHandler clientHandler) {
		this.serviceProviderMap.put(name, clientHandler);
		ObjectLock lock = ObjectLock.getLock(name);
		lock.release();
	}
	public AntContext getContext() {
		return this.antContext;
	}
	public AntContextConfigure getContextConfigure() {
		return this.antContext.getContextConfigure();
	}
	public AntRegisterService getAntRegisterServcie() {
		return antRegisterServcie;
	}
	public MessageQueue getMessageQueue() {
		return messageQueue;
	}
	public void handlerMapping(SocketChannel socketChannel, AntClientHandler handler) {
		this.handlerMapping.put(socketChannel, handler);
	}
}
