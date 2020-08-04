package com.yanan.framework.ant.protocol.ant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.framework.ant.abstracts.ClientInstance;
import com.yanan.framework.ant.abstracts.ClientService;
import com.yanan.framework.ant.exception.AntInitException;
import com.yanan.framework.ant.model.AntProviderSummary;
import com.yanan.framework.ant.service.AntRuntimeService;
import com.yanan.framework.plugin.annotations.Register;

@Register(signlTon=true)
public class AntClientService implements ClientService{
	private static Logger logger = LoggerFactory.getLogger(AntClientService.class);
	private AntRuntimeService runtimeService;
	private SelectorRunningService selectorRunningService;
	private Thread selectorThread;
	private Object selectObjectLock = new Object();
	private int serverPort;
	/**
	 * 服务端注册服务
	 */
	private AntRegisterService antRegisterServcie;
	/**
	 * 客户端注册服务
	 */
	private AntClientRegisterService antClientRegisterService;
	/**
	 * 所有通道和连接处理器的映射
	 */
	private Map<SocketChannel,ClientInstance> handlerMapping;
	public AntClientService(AntRuntimeService runtimeService) {
		this.runtimeService = runtimeService;
		handlerMapping = new ConcurrentHashMap<SocketChannel,ClientInstance>();
		selectorRunningService = new SelectorRunningService(this);
		this.antRegisterServcie = new AntRegisterService(runtimeService);
		this.antClientRegisterService = new AntClientRegisterService(runtimeService);
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
	public void clientService(AntProviderSummary providerSummary) throws IOException {
		logger.debug("connection to server "+providerSummary);
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.connect(new InetSocketAddress(providerSummary.getHost(),providerSummary.getPort()));
		socketChannel.register(selectorRunningService.getSelector(), SelectionKey.OP_CONNECT,providerSummary);
	}
	@Override
	public AntRuntimeService getRuntimeService() {
		return runtimeService;
	}
	@Override
	public void startProvider() {
			try {
				logger.debug("enable ant provider service");
				logger.debug("ant provider service name:"+runtimeService.getContextConfigure().getName());
				ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
				String portStr = runtimeService.getContextConfigure().getPort();
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
	public void handler(SocketChannel socketChannel, AntClientInstance clientInstance) {
		this.handlerMapping.put(socketChannel, clientInstance);
	}
	public ClientInstance getHandler(SocketChannel socketChannel) {
		return this.handlerMapping.get(socketChannel);
	}
	@Override
	public int getServerPort() {
		return serverPort;
	}
	public AntRegisterService getAntRegisterServcie() {
		return antRegisterServcie;
	}
	public AntClientRegisterService getAntClientRegisterService() {
		return antClientRegisterService;
	}
}