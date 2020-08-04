package com.yanan.framework.ant.utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import com.yanan.framework.ant.exception.AntMessageLockException;
import com.yanan.framework.ant.exception.AntRequestTimeoutException;
import com.yanan.framework.ant.exception.AntResponseException;
import com.yanan.framework.ant.exception.AntRuntimeException;
import com.yanan.framework.ant.model.AntMessagePrototype;
import com.yanan.framework.ant.service.AntRuntimeService;
import com.yanan.framework.ant.type.MessageType;

public class MessageQueue {
	/**
	 * 锁定队列
	 */
	private ConcurrentHashMap<Integer,AntMessageLock> lockQueue = new ConcurrentHashMap<Integer,AntMessageLock>();
	private ConcurrentLinkedQueue<AntMessageLock> lockList = new ConcurrentLinkedQueue<AntMessageLock>();
	private LockTimeoutService lockTimeoutChecker = new LockTimeoutService();
	private AntRuntimeService antRuntimeService;
	public MessageQueue(AntRuntimeService antRuntimeService) {
		super();
		this.antRuntimeService = antRuntimeService;
	}
	public MessageQueue(long timeout) {
		super();
	}
	
	/**
	 * 维护一个去查看消息时间的线程
	 */
	class LockTimeoutService implements Runnable{
		private boolean available;
		
		@Override
		public void run() {
			while(available){
				AntMessageLock lock;
				while(!lockList.isEmpty()){
					lock =lockList.peek();
					if(lock !=null && System.currentTimeMillis()- lock.getTimes() > lock.getMessage().getTimeout()) {
						lockList.poll();
						notifyException(lock.getMessage().getRID(),new AntRequestTimeoutException("rpc request timeout ["+lock.getMessage().getTimeout()+"ms] at requestID "+lock.getMessage().getRID()+" when request service ["+
						lock.getMessage().getService()+"]"));	
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
	/**
	 * 将请求添加到这里来
	 * @param msg
	 */
	public void addRequest(AntMessagePrototype msg) {
		if(msg==null || msg.getType() != MessageType.REQUEST)
			return;
		AntMessageLock lock = AntMessageLock.getLock(msg);
		lockList.add(lock);
		lockQueue.put(msg.getRID(),lock);
		lockTimeoutChecker.enable();
	}
	public Object getResult(AntMessagePrototype msg) {
		AntMessageLock lock = getLock(msg.getRID());
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
	public AntMessageLock getLock(int ruid){
		return this.lockQueue.get((Integer)ruid);
	}
	public void removeQueue(AntMessageLock lock){
		lockList.remove(lock);
		lockQueue.remove((Integer)lock.getMessage().getRID());
	}
	/**
	 * 通知请求出现异常
	 * @param ruid
	 * @param t
	 */
	public void notifyException(int ruid,Throwable t){
		AntMessageLock lock = getLock(ruid);
		if(lock==null)
			return;
		lock.setException(t);
		lock.unLock();
	}
	/**
	 * 获取所有的未处理完成的消息
	 * @param ruid
	 * @return 
	 */
	public List<AntMessagePrototype> getAllProcessingMessage(String serviceName){
		return lockList.stream()
				.filter(lock->serviceName.equals(lock.getMessage().getService()))
				.map(lock->lock.getMessage())
				.collect(Collectors.toList());
	}
	/**
	 * 通知请求已有结果
	 * @param msg
	 */
	public void notifyRequest(int ruid,AntMessagePrototype message){
		AntMessageLock lock = getLock(ruid);
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
	public AntRuntimeService getAntRuntimeService() {
		return antRuntimeService;
	}
}