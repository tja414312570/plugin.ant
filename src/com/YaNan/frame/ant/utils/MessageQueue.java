package com.YaNan.frame.ant.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.YaNan.frame.ant.AntContext;
import com.YaNan.frame.ant.exception.AntMessageLockException;
import com.YaNan.frame.ant.exception.AntRequestTimeoutException;
import com.YaNan.frame.ant.exception.AntResponseException;
import com.YaNan.frame.ant.exception.AntRuntimeException;
import com.YaNan.frame.ant.model.AntMessagePrototype;

public class MessageQueue {
	private volatile static MessageQueue queue;
	/**
	 * 锁定队列
	 */
	private ConcurrentHashMap<Integer,Lock> lockQueue = new ConcurrentHashMap<Integer,Lock>();
	private ConcurrentLinkedQueue<Lock> lockList = new ConcurrentLinkedQueue<Lock>();
	private LockTimeoutService lockTimeoutChecker = new LockTimeoutService();
	private long timeout = AntContext.getContext().getContextConfigure().getTimeout();
	/**
	 * 维护一个去查看消息时间的线程
	 */
	class LockTimeoutService implements Runnable{
		private boolean available;
		
		@Override
		public void run() {
			while(available){
				Lock lock;
				while(!lockList.isEmpty()){
					lock =lockList.peek();
					if(lock !=null && System.currentTimeMillis()- lock.getTimes() > timeout) {
						lockList.poll();
						notifyException(lock.getMessage().getRID(),new AntRequestTimeoutException("rpc request timeout at requestID "+lock.getMessage().getRID()+" when request service "));	
					}else {
						break;
					}
				}
				if(lockList.isEmpty())
					this.available = false;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		public void enable(){
			if(available)
				return;
			this.available=true;
			Thread thread = new Thread(this);
			thread.setName("RPC-Message-Lock-Timeout-Checker");
			thread.setDaemon(true);
			thread.start();
		}
	}
	public static MessageQueue getQueue(){
		if(queue == null){
			synchronized (MessageQueue.class) {
				if(queue == null)
					queue = new MessageQueue();
			}
		}
		return queue;
	}
	/**
	 * 将请求添加到这里来
	 * @param msg
	 */
	public void addRequest(AntMessagePrototype msg) {
		if(msg==null)
			return;
		Lock lock = Lock.getLock(msg);
		lockList.add(lock);
		lockQueue.put(msg.getRID(),lock);
		lockTimeoutChecker.enable();
	}
	public Object getResult(AntMessagePrototype msg) {
		Lock lock = getLock(msg.getRID());
		if(lock==null) 
			throw new AntRuntimeException("could not found message lock for "+msg.getRID());
		try {
			lock.lock();
		} catch (Exception e) {
			throw new AntMessageLockException("lock rpc request failed where "+msg,e);
		}finally {
			this.removeQueue(lock);
		}
		if(lock.getException()!=null)
			throw new AntResponseException(lock.getException());
		
		return lock.getResult();
	}
	public Lock getLock(int ruid){
		return this.lockQueue.get((Integer)ruid);
	}
	public void removeQueue(Lock lock){
		lockList.remove(lock);
		lockQueue.remove((Integer)lock.getMessage().getRID());
	}
	/**
	 * 通知请求出现异常
	 * @param ruid
	 * @param t
	 */
	public void notifyException(int ruid,Throwable t){
		Lock lock = getLock(ruid);
		if(lock==null)
			return;
		lock.setException(t);
		lock.unLock();
	}
	/**
	 * 通知请求已有结果
	 * @param msg
	 */
	public void notifyRequest(int ruid,AntMessagePrototype message){
		Lock lock = getLock(ruid);
		if(lock==null)
			return;
		try {
			message.setInvokeMethod(lock.getMessage().getInvokeMethod());
			message.decode();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			notifyException(ruid, e);
//			AntContext.getContext().getManager().getListener().OnException(RPCService.getManager(), new RPCRuntimeException("failed to decode message;"+message,e));
		}
		if(message.getInvokeMethod().getReturnType().isArray()) {
			lock.setResult(message.getInvokeParmeters());
		}else {
			lock.setResult(message.getInvokeParmeters().length>0?message.getInvokeParmeters()[0]:null);
		}
		lock.unLock();
	}
	public long getTimeout() {
		return timeout;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
