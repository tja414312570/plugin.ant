package com.YaNan.frame.ant.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.AntContext;
import com.YaNan.frame.ant.abstracts.AbstractProcess;
import com.YaNan.frame.ant.exception.AntClientHandlerOverFlow;
import com.YaNan.frame.ant.exception.AntInitException;
import com.YaNan.frame.ant.exception.AntRequestException;
import com.YaNan.frame.ant.exception.ServiceNotRunningException;
import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.implement.AntChannelProcess;
import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.ant.utils.MessageProcesser;
import com.YaNan.frame.ant.utils.MessageQueue;

public class AntRuntimeService {
	volatile private static AntRuntimeService runtimeService;
	private AntContext antContext;
	private Selector selector;
	private boolean daemon = true;
	/**
	 * 客户端注册服务
	 */
	private AntRegisterService antClientRegisterService;
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
	/**
	 * 此handler为服务中心handler
	 */
	private AntClientHandler queenClientHandler;
	private static Logger logger = LoggerFactory.getLogger(AntRuntimeService.class);
	private volatile boolean available = false;
	private static final int MAX_ID_NUM = Integer.MAX_VALUE;
	private AtomicInteger idCount = new AtomicInteger();
	private AtomicInteger clientIdCount = new AtomicInteger();
	private Thread selectorThread;
	public int getRID(){
		int rid = idCount.incrementAndGet();
		if(rid >= MAX_ID_NUM)
			idCount.set(0);
		return rid;
	}
	private AntRuntimeService() {
		antContext = AntContext.getContext();
		this.antClientRegisterService = AntRegisterService.getInstance();
		handlerMapping = new HashMap<SocketChannel,AntClientHandler>();
		this.serviceProviderMap = new HashMap<String,AntClientHandler>();
		//如果没有设置处理线程数，设置默认线程数
		createProcessExector();
		//创建选择器
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		selectorRunningService = new SelectorRunningService(this);
	}
	private void createProcessExector() {
		BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>(AntContext.getContext().getContextConfigure().getTasksize());
		if(antContext.getContextConfigure().getProcess()>0){
			if(antContext.getContextConfigure().getMaxProcess()<antContext.getContextConfigure().getProcess())
				antContext.getContextConfigure().setMaxProcess(antContext.getContextConfigure().getProcess()<<1);
			this.processExecutor = new ThreadPoolExecutor(antContext.getContextConfigure().getProcess(),
					antContext.getContextConfigure().getMaxProcess(), 
					antContext.getContextConfigure().getTimeout(), 
					TimeUnit.MILLISECONDS, 
					blockingQueue,
					new AntThreadFactory(),
					new ThreadPoolExecutor.CallerRunsPolicy());
		}else {
			this.processExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
					Runtime.getRuntime().availableProcessors()<<1,
					antContext.getContextConfigure().getTimeout(),
					TimeUnit.MILLISECONDS,
					blockingQueue,
					new AntThreadFactory(),
					new ThreadPoolExecutor.CallerRunsPolicy());
		}
	}
	public void setQueenHandler(AntClientHandler handler) {
		queenClientHandler = handler;
	}
	public AntClientHandler getQueenHandler() {
		return queenClientHandler;
	}
	public static AntRuntimeService getAntRuntimeService() {
		if(runtimeService == null) {
			synchronized (AntRuntimeService.class) {
				if(runtimeService == null) {
					runtimeService = new AntRuntimeService();
				}
			}
		}
		return runtimeService;
	}
	public static class SelectorRunningService implements Runnable{
		private AntRuntimeService runtimeService;
		public SelectorRunningService(AntRuntimeService antRuntimeService) {
			runtimeService  = antRuntimeService;
		}
		@Override
		public void run() {
			SocketChannel socketChannel;
			AntClientHandler handler;
				  while(true) {
					  try {
							if(runtimeService.selector.select() == 0) {
								continue;
							}
							Iterator<SelectionKey> keyIter = runtimeService.selector.selectedKeys().iterator();
				            while (keyIter.hasNext()) {
				                SelectionKey key = keyIter.next();
				                //客户端连接到此设备
				                if (key.isAcceptable()){  
				                	socketChannel = ((ServerSocketChannel)key.channel()).accept();
				        	        socketChannel.configureBlocking(false);
				        	        socketChannel.register(key.selector(), SelectionKey.OP_READ);
				        	        handler = new AntClientHandler(socketChannel);
				        	        runtimeService.executeProcess(new AntChannelProcess(handler,SelectionKey.OP_ACCEPT,key));
//				                	AntClientRegisterService.getInstance().register(handler);
				                	runtimeService.handlerMapping.put(socketChannel, handler);
				                	keyIter.remove();
				                	continue;
				                }  
				                socketChannel = (SocketChannel) key.channel();
				                //连接到目标
				                if(key.isConnectable()) {
				                	handler = new AntClientHandler(socketChannel);
				                	key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				                	runtimeService.handlerMapping.put(socketChannel, handler);
				                	runtimeService.executeProcess(new AntChannelProcess(handler,SelectionKey.OP_CONNECT,key));
				                }
				                handler = runtimeService.handlerMapping.get(socketChannel);
				                if(handler == null) {
				                	socketChannel.close();
				                }
				                //可读
				                if(key.isReadable()) {
				                	runtimeService.executeProcess(new AntChannelProcess(handler,SelectionKey.OP_READ,key));
//				                	handler.handleRead(key);
				                }
				                //可写
				                if(key.isWritable()) {
//				                	handler.handlWrite(key);
				                }
				                keyIter.remove(); 
				            }
					} catch (IOException e) {
						e.printStackTrace();
					}
				 
			}
		}
		
	}
	public void registerHandler(SocketChannel socketChannel) throws ClosedChannelException {
		 socketChannel.register(selector, SelectionKey.OP_CONNECT);
	}
	public void start()  {
		try {
			//开启提供者服务
			if(antContext.getContextConfigure().getServerPort()>0) {
				startProvider();
			}
			//创建注册中心连接
			if(antContext.getContextConfigure().getHost()!=null && antContext.getContextConfigure().getPort()>0 && !antContext.getContextConfigure().getHost().trim().equals("")) {
				startAnt();
			}
			checkAndStartSelectorThread();
		} catch (IOException | InterruptedException e) {
			throw new AntInitException(e);
		}
	}
	public void startAnt() throws InterruptedException, IOException {
		logger.debug("start ant service");
		logger.debug("queen port:"+antContext.getContextConfigure().getPort());
		clientProvider(null, antContext.getContextConfigure().getHost(),
					antContext.getContextConfigure().getPort());
	}
	private void checkAndStartSelectorThread() {
		if(selectorThread == null || !selectorThread.isAlive()) {
			synchronized (this) {
				selectorThread = new Thread(selectorRunningService);
				selectorThread.setName("Ant Runtime Selector Thread");
				selectorThread.setDaemon(daemon);
				selectorThread.start();
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
			MessageQueue.getQueue().addRequest(message);
//			logger.debug("request to '"+handler.getSocketChannel().getRemoteAddress()+"' Ant proxy："+message+"，lock："+lock);
			//写入数据
			if(lock)
				handler.writeAndLock(message);
			else
				handler.write(message);
			//如果调用有返回值
//			if(!message.getInvokeMethod().getReturnType().equals(void.class))
//							//给消息加锁
				return MessageQueue.getQueue().getResult(message);
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
	public AntRegisterService getAntClientRegisterService() {
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
	public void started() {
		if(!this.available) {
			synchronized (this) {
				this.notifyAll();
			}
		}
	}
	/**
	 * 开启服务提供
	 */
	public void startProvider() {
		try {
			logger.debug("start ant provider service");
			logger.debug("ant provider service name:"+antContext.getContextConfigure().getName());
			logger.debug("listening server port:"+antContext.getContextConfigure().getServerPort());
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.bind(
					new InetSocketAddress(
							antContext.getContextConfigure().getServerPort()));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
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
	 */
	public void clientProvider(AntProviderSummary providerSummary,String host,int port) {
		try {
			logger.debug("client to host:"+host);
			logger.debug("client to port:"+port);
			SocketChannel socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(new InetSocketAddress(host,port));
			socketChannel.register(selector, SelectionKey.OP_CONNECT,providerSummary);
			checkAndStartSelectorThread();
			if (!this.available) {
				synchronized (this) {
					this.wait();
				}
			} 
		} catch (Exception e) {
		}
	}
	/**
	 * 获取一个连接服务
	 * @param name
	 * @return
	 */
	public AntClientHandler getClientHandler(String name) {
		return this.serviceProviderMap.get(name);
	}
	public void addClientHandler(String name, AntClientHandler clientHandler) {
		System.out.println("添加:"+name);
		this.serviceProviderMap.put(name, clientHandler);
		System.out.println(this.serviceProviderMap);
	}
	
}
