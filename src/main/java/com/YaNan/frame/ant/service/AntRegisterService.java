package com.YaNan.frame.ant.service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.YaNan.frame.ant.abstracts.AntAbstractRegisterHandler;
import com.YaNan.frame.ant.exception.AntRegisterRuntimeException;

/**
 * Ant注册服务
 * @author yanan
 *
 */
public class AntRegisterService {
//	private static Map<AntRuntimeService,AntRegisterService> instanceMap = new ConcurrentHashMap<>();
	/**
	 * 注册队列
	 */
	private volatile ConcurrentLinkedQueue<AntAbstractRegisterHandler> waitRegisterList = new ConcurrentLinkedQueue<AntAbstractRegisterHandler>();
	/**
	 * 连接队列
	 */
	private volatile ConcurrentLinkedQueue<AntAbstractRegisterHandler> waitConnectionList = new ConcurrentLinkedQueue<AntAbstractRegisterHandler>();
	/**
	 * 注册检查线程
	 */
	private Thread registerCheckThread;
	/**
	 * 注册检查服务
	 */
	private AntRegisterCheckService registerCheckService;
	/**
	 * 注册锁
	 */
	private boolean lock;
	/**
	 * 注册服务可用
	 */
	private volatile boolean available = true;
	/**
	 * 运行时服务
	 */
	private AntRuntimeService runtimeService;
	public AntRegisterService(AntRuntimeService runtimeService) {
		registerCheckService = new AntRegisterCheckService(this);
		this.runtimeService = runtimeService;
	}
	/**
	 * 启用注册检查线程
	 */
	private void enableRegisterCheck() {
		if(registerCheckThread == null || !registerCheckThread.isAlive()) {
			synchronized (registerCheckService) {
				if(registerCheckThread == null || !registerCheckThread.isAlive()) {
					registerCheckThread = new Thread(registerCheckService);
					registerCheckThread.setName("Ant-Regist-Check-Server");
					registerCheckThread.setDaemon(true);
					registerCheckThread.start();
				}
			}
		}
	}
//	public static AntRegisterService getInstance(AntRuntimeService runtimeService) {
//		AntRegisterService instance = instanceMap.get(runtimeService);
//		if(instance == null) {
//			synchronized (runtimeService) {
//				instance = instanceMap.get(runtimeService);
//				if(instance == null) {
//					instance = new AntRegisterService(runtimeService);
//					instanceMap.put(runtimeService, instance);
//				}
//			}
//		}
//		return instance;
//	}
	/**
	 * 获取代注册列表
	 * @return
	 */
	public LinkedList<AntAbstractRegisterHandler> getWaitRegisterList() {
		return new LinkedList<AntAbstractRegisterHandler>(this.waitRegisterList);
	}
	/**
	 * 获取待连接列表
	 * @return
	 */
	public LinkedList<AntAbstractRegisterHandler> getWaitConnectionList() {
		return new LinkedList<AntAbstractRegisterHandler>(this.waitConnectionList);
	}
	/**
	 * 检查列表
	 */
	public void check() {
		//获取当前时间
		long now = System.currentTimeMillis();
		AntAbstractRegisterHandler handler;
		//锁定待连接列表
		synchronized (waitConnectionList) {
			//迭代
			Iterator<AntAbstractRegisterHandler> iterator = waitConnectionList.iterator();
			while(iterator.hasNext()) {
			handler = iterator.next();
				try {
					//完成连接
					handler.getClientHandler().getRuntimeService().executeProcess(handler);
					//加入到注册检测集合
					waitRegisterList.add(handler);
					//重置注册时间
					handler.setRecoderTime(System.currentTimeMillis()+	handler.getClientHandler().getRuntimeService().getContextConfigure().getCheckTime());
					iterator.remove();
				} catch (Exception e) {
					iterator.remove();
					handler.getClientHandler().close(e);
				}
				//注册时间判断
				if(now - handler.getRecoderTime()>
				handler.getClientHandler().getRuntimeService().getContextConfigure().getTimeout()) {
					//如果超时，移除并通知handler
					handler.getClientHandler().connectTimeout();
					iterator.remove();
				}
			}
		}
		//锁定列表
		synchronized (waitRegisterList) {
			//迭代
			Iterator<AntAbstractRegisterHandler> iterator = waitRegisterList.iterator();
			while(iterator.hasNext()) {
				handler = iterator.next();
				//注册时间判断
				if(now - handler.getRecoderTime()>
				handler.getClientHandler().getRuntimeService().getContextConfigure().getTimeout()) {
					//如果超时，移除并通知handler
					handler.getClientHandler().registTimeout();
					iterator.remove();
				}
			}
		}
		if(this.waitConnectionList.size() == 0 && this.waitRegisterList.size() == 0 )
			registerCheckService.close();
	}
	
	public static class AntRegisterCheckService implements Runnable{
		private volatile boolean available;
		private AntRegisterService registerService;
		public AntRegisterCheckService(AntRegisterService registerService) {
			this.registerService = registerService;
			available = true;
		}
		@Override
		public void run() {
			while(available) {
				registerService.check();
				try {
					Thread.sleep(registerService.runtimeService.getContextConfigure().getCheckTime());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		public void close() {
			available = false;
		}
		
	}
	/**
	 * 获取注册锁
	 */
	public void tryAcquire() {
		if(lock) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(!available)
			throw new AntRegisterRuntimeException("Regist service invalid");
	}
	/**
	 * 给注册服务加锁
	 */
	public void lock() {
		synchronized (this) {
			this.lock = true;
		}
	}
	/**
	 * 释放锁
	 */
	public void releaseLock() {
		synchronized (this) {
			this.lock = false;
		}
	}
	/**
	 * 将注册处理器添加到注册列表
	 * @param handler
	 */
	public void register(AntAbstractRegisterHandler antAbstractRegisterHandler) {
		tryAcquire();
		if(!this.waitConnectionList .contains(antAbstractRegisterHandler)) {
			this.waitConnectionList.add(antAbstractRegisterHandler);
			this.enableRegisterCheck();
		}
	}
	/**
	 * 注册并锁定注册中心
	 * 锁定之后不能加入其它注册内容
	 * @param AntAbstractRegisterHandler
	 */
	public void registerAndLock(AntAbstractRegisterHandler AntAbstractRegisterHandler) {
		this.register(AntAbstractRegisterHandler);
		this.lock();
	}
	/**
	 * 注册成功
	 * @param AntAbstractRegisterHandler
	 */
	public void removeRegister(AntAbstractRegisterHandler antAbstractRegisterHandler) {
		this.waitConnectionList.remove(antAbstractRegisterHandler);
		this.waitRegisterList.remove(antAbstractRegisterHandler);
		if(lock) {
			synchronized (this) {
				lock = false;
				this.notifyAll();
			}
		}
	}
	/**
	 * 服务可用
	 * @return
	 */
	public boolean isAvailable() {
		return available;
	}
	public synchronized void setAvailable(boolean available) {
		this.available = available;
	}
}
