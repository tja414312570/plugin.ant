
package com.YaNan.frame.ant.handler;

import java.io.IOException;
import java.io.WriteAbortedException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.AntContextConfigure;
import com.YaNan.frame.ant.abstracts.AbstractProcess;
import com.YaNan.frame.ant.exception.AntInitException;
import com.YaNan.frame.ant.exception.AntMessageResolveException;
import com.YaNan.frame.ant.exception.AntRuntimeException;
import com.YaNan.frame.ant.interfaces.BufferReady;
import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.ant.service.AntRuntimeService;
import com.YaNan.frame.ant.type.BufferType;
import com.YaNan.frame.ant.type.ClientType;
import com.YaNan.frame.ant.type.MessageType;
import com.YaNan.frame.ant.utils.MessageProcesser;
import com.YaNan.frame.ant.utils.ObjectLock;

/**
 * 一个通道对应一个handler
 * @author yanan
 *
 */
public class AntClientHandler {
	@Override
	public String toString() {
		return "AntClientHandler [id=" + id + ", remoteAddress=" + remoteAddress
				+ ", runtimeService=" + runtimeService + ", clientType=" + clientType
				+ ", startTime=" + new Date(clientTime) +"]";
	}
	private static Logger logger = LoggerFactory.getLogger(AntClientHandler.class);
	/**
	 * 存储当前线程的连接绑定对象
	 */
	private static InheritableThreadLocal<AntClientHandler> clientHandleLocal = new InheritableThreadLocal<AntClientHandler>();
	/**
	 * 连接开始时间
	 */
	private long clientTime;
	/**
	 * 服务通道
	 */
	private SocketChannel socketChannel;
	/**
	 * 客户端类型
	 */
	private ClientType clientType;
	/**
	 * 消息解析包
	 */
	private AntMessageHandler messageHandler;
	/**
	 * 执行线程
	 */
	private Thread executeThread;
	/**
	 * 缓冲区可用
	 */
	private BufferReady messageWriteHandler;
	/**
	 * 写锁
	 */
	private volatile boolean writeLock;
	/**
	 * 一个Map，用来存储当前连接的属性
	 */
	private Map<Object,Object> attributes;
	
	private Throwable closeCause;
	
	private final int id;
	
	private SocketAddress remoteAddress;
	private AntRuntimeService runtimeService;
	public AntRuntimeService getRuntimeService() {
		return runtimeService;
	}
	/**
	 * 获取当前连接的所有属性
	 * @return
	 */
	public Map<Object, Object> getAttributes() {
		return this.attributes;
	}
	/**
	 * 获取当前连接的某属性
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Object key) {
		return (T) this.attributes.get(key);
	}
	/**
	 * 设置当前连接的属性
	 * @param key
	 * @param value
	 */
	public void setAttribute(Object key,Object value) {
		this.attributes.put(key, value);
	}
	/**
	 * 判断属性集合是否包含某属性，此属性可能属性值为null
	 * @param key
	 * @return
	 */
	public boolean containsAttribute(Object key) {
		return this.attributes.containsKey(key);
	}
	
	/**
	 * 获取当前线程的连接对象
	 * @return
	 */
	public static AntClientHandler getHandler() {
		return clientHandleLocal.get();
	}
	public static AntClientHandler setHandler(AntClientHandler clientHandler) {
		AntClientHandler tempClientHandler = clientHandleLocal.get();
		clientHandleLocal.set(clientHandler);
		return tempClientHandler;
	}
	/**
	 * 连接处理器的构造方法
	 * @param socketChannel
	 * @param runtimeService 
	 */
	public AntClientHandler(SocketChannel socketChannel, AntRuntimeService runtimeService) {
		/**
		 * 当前执行器的创建时间
		 */
		this.runtimeService  = runtimeService;
		this.clientTime = System.currentTimeMillis();
		this.id = runtimeService.getClientId();
		//设置当前线程
		clientHandleLocal.set(this);
		//通道
		this.socketChannel = socketChannel;
		//属性集合
		attributes = new HashMap<Object,Object>();
		//消息处理器
		this.messageHandler = AntMessageHandler.getHandler(this);
		//设置消息处理器的缓冲区
		AntContextConfigure config = runtimeService.getContextConfigure();
		messageHandler.setMaxBufferSize(config.getBufferMaxSize());
		if (config.getBufferType() == BufferType.DIRECT) {
			messageHandler.setReadBuffer(ByteBuffer.allocateDirect(config.getBufferSize()));
			messageHandler.setWriteBuffer(ByteBuffer.allocateDirect(config.getBufferSize()));
		} else {
			messageHandler.setReadBuffer(ByteBuffer.allocate(config.getBufferSize()));
			messageHandler.setWriteBuffer(ByteBuffer.allocate(config.getBufferSize()));
		}
		try {
			this.setRemoteAddress(socketChannel.getRemoteAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//当前执行器所在的线程
		executeThread = Thread.currentThread();
		//缓冲区数据准备好以后写入通道
		this.messageWriteHandler = new BufferReady() {
			@Override
			public void bufferReady(ByteBuffer buffer) throws WriteAbortedException {
				writeToChannel(buffer);
			}
		};
	}
	/**
	 * 将缓冲区数据写入到通道
	 * @param buffer
	 * @throws WriteAbortedException
	 */
	protected void writeToChannel(ByteBuffer buffer) throws WriteAbortedException {
		try {
			buffer.flip();
			socketChannel.write(buffer);
			buffer.compact();
		} catch (IOException e) {
			runtimeService.tryRecoveryServiceAndNotifyDiscoveryService(this);
			throw new WriteAbortedException("ant message write failed!", e);
		}
		buffer.flip();
		buffer.clear();
	}
	public long getClientTime() {
		return clientTime;
	}
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}
	public ClientType getClientType() {
		return clientType;
	}
	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}
	public synchronized void handleRead(SelectionKey key) {
		if(closeCause != null) {
			key.cancel();
			return;
		}
		clientHandleLocal.set(this);
		executeThread = Thread.currentThread();
		try {
			while (socketChannel.read(messageHandler.getReadBuffer()) > 0) {
				messageHandler.handleRead();
			}
			AntMessagePrototype message;
			while((message = messageHandler.getMessage())!=null) {
				logger.debug("accept message from '"+this.getRemoteAddress()+"':\":"+message);
				runtimeService.execute(new MessageProcesser(message, this));
			}
		} catch (AntMessageResolveException amre) {
			logger.error("failed to read buffer",amre);
			if(this.clientType == ClientType.Provider || this.clientType == ClientType.Queen ) {
				AntMessagePrototype amp = new AntMessagePrototype();
				amp.setType(MessageType.EXCEPTION);
				amp.setRID(amre.getRID());
				amp.setInvokeClass(AntMessageResolveException.class);
				amp.setInvokeParmeters(amre.getMessage());
				this.write(amp);
			}
			key.cancel();
		} catch (Throwable e) {
			key.cancel();
			logger.error("failed to read buffer",e);
			this.close(e);
			AntClientHandler self = this;
			runtimeService.executeProcess(new AbstractProcess() {
				@Override
				public void execute() {
					runtimeService.tryRecoveryServiceAndNotifyDiscoveryService(self);
				}
			});
		}
	}
	public void handlWrite(SelectionKey key) {
		clientHandleLocal.set(this);
		executeThread = Thread.currentThread();
	}
	public static AntClientHandler removeHandler() {
		AntClientHandler handle = clientHandleLocal.get();
		clientHandleLocal.remove();
		return handle;
	}
	public AntMessageHandler getMessageHandler() {
		return messageHandler;
	}
	public void setMessageHandler(AntMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	public Thread getExecuteThread() {
		return executeThread;
	}
	public void write(AntMessagePrototype message) {
		tryAcquire();
		logger.debug("write message to '"+this.getRemoteAddress()+"':"+message);
		this.messageHandler.write(message,this.messageWriteHandler);
	}
	/**
	 * 释放写锁
	 */
	public void releaseWriteLock() {
		writeLock = false;
		synchronized (this) {
			this.notifyAll();
		}
	}
	public boolean isWriteLocked() {
		return writeLock;
	}
	/**
	 * 写入消息并加锁
	 * @param message
	 */
	public void writeAndLock(AntMessagePrototype message) {
		this.write(message);
		this.writeLock();
	}
	/**
	 * 将写入通道加锁
	 */
	private void writeLock() {
		synchronized (this) {
			writeLock = true;
		}
	}
	public void registTimeout() {
		this.close(new AntInitException("regist timeout for "+this.getId()));
	}
	public void connectTimeout() {
		this.close(new AntInitException("connect timeout"));
	}
	/**
	 * 获取服务的锁
	 */
	public void tryAcquire() {
		if(writeLock) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * 关闭服务
	 * @param cause 关闭原因
	 */
	public void close(Throwable cause) {
		try {
			if(this.closeCause != null)
				return;
			AntProviderSummary summary = this.getAttribute(AntProviderSummary.class);
    		if(summary != null) {
    			ObjectLock.getLock(summary.getName()).release();
    		}
			logger.debug("the socket channel close，info :"+socketChannel.getRemoteAddress().toString()+"\n cause:"+cause.getMessage(),cause);
			this.closeCause = cause;
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("an error has occurred when close socket channel",e);
		}
	}
	/**
	 * 关闭服务
	 * @param cause
	 */
	public void close(String cause) {
		this.close(new AntRuntimeException(cause));
	}
	/**
	 * 获取服务关闭的原因
	 * @return
	 */
	public Throwable getCloseCause() {
		return closeCause;
	}
	public int getId() {
		return id;
	}
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
}
