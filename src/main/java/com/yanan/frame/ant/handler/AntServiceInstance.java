package com.yanan.frame.ant.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.ant.abstracts.ClientInstance;
import com.yanan.frame.ant.exception.AntRuntimeException;
import com.yanan.frame.ant.model.AntMessagePrototype;
import com.yanan.frame.ant.model.AntProviderSummary;
import com.yanan.frame.ant.protocol.ant.ProcessProvider;
import com.yanan.frame.ant.service.AntRuntimeService;
import com.yanan.frame.ant.type.ClientType;
import com.yanan.frame.ant.utils.ObjectLock;

/**
 * 一个通道对应一个handler
 * @author yanan
 *
 */
public class AntServiceInstance {
	@Override
	public String toString() {
		return "AntClientHandler [id=" + id 
				+ ", runtimeService=" + runtimeService + ", clientType=" + clientType
				+ ", startTime=" + new Date(clientTime) +"]";
	}
	private static Logger logger = LoggerFactory.getLogger(AntServiceInstance.class);
	/**
	 * 存储当前线程的连接绑定对象
	 */
	private static InheritableThreadLocal<AntServiceInstance> clientHandleLocal = new InheritableThreadLocal<AntServiceInstance>();
	/**
	 * 连接开始时间
	 */
	private long clientTime;
	/**
	 * 客户端类型
	 */
	private ClientType clientType;
	/**
	 * 消息解析包
	 */
	private ClientInstance clientInstance;
	public ClientInstance getClientInstance() {
		return clientInstance;
	}
	/**
	 * 执行线程
	 */
	private Thread executeThread;
	/**
	 * 写锁
	 */
	private volatile boolean writeLock;
	/**
	 * 一个Map，用来存储当前连接的属性
	 */
	private Map<Object,Object> attributes;
	
	private final int id;
	
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
	public static AntServiceInstance getServiceInstance() {
		return clientHandleLocal.get();
	}
	static AntServiceInstance setHandler(AntServiceInstance clientHandler) {
		AntServiceInstance tempClientHandler = clientHandleLocal.get();
		clientHandleLocal.set(clientHandler);
		return tempClientHandler;
	}
	/**
	 * 连接处理器的构造方法
	 * @param socketChannel
	 * @param runtimeService 
	 */
	public AntServiceInstance(AntRuntimeService runtimeService,ClientInstance clientInstance) {
		/**
		 * 当前执行器的创建时间
		 */
		this.runtimeService  = runtimeService;
		this.clientTime = System.currentTimeMillis();
		this.id = runtimeService.getClientId();
		//设置当前线程
		clientHandleLocal.set(this);
		this.clientInstance = clientInstance;
		//通道
//		this.socketChannel = socketChannel;
		//属性集合
		attributes = new HashMap<Object,Object>();
		//消息处理器
//		this.messageHandler = AntMessageHandler.getHandler(this);
		//当前执行器所在的线程
		executeThread = Thread.currentThread();
		//缓冲区数据准备好以后写入通道
	}
	public long getClientTime() {
		return clientTime;
	}
	public ClientType getClientType() {
		return clientType;
	}
	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}
	public synchronized void onMessage(AntMessagePrototype message) {
		clientHandleLocal.set(this);
		executeThread = Thread.currentThread();
		runtimeService.execute(ProcessProvider.get(message, this));
	}
	public static AntServiceInstance removeHandler() {
		AntServiceInstance handle = clientHandleLocal.get();
		clientHandleLocal.remove();
		return handle;
	}
	public Thread getExecuteThread() {
		return executeThread;
	}
	public void write(AntMessagePrototype message) {
		tryAcquire();
		this.clientInstance.write(message);
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
	 * @param cause
	 */
	public void close(String cause) {
		this.close(new AntRuntimeException(cause));
	}
	public void close(Throwable throwable) {
		AntProviderSummary summary = this.getAttribute(AntProviderSummary.class);
		if(summary != null) {
			ObjectLock.getLock(summary.getName()).release();
		}
		clientInstance.close(throwable);
	}
	public int getId() {
		return id;
	}
}