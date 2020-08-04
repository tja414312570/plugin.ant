package com.yanan.framework.ant.protocol.ant;

import java.util.Iterator;
import java.util.LinkedList;

import com.yanan.framework.ant.service.AntRuntimeService;

public class AntClientRegisterService {
//	private static AntClientRegisterService antClientRegisterService;
	private LinkedList<AntClientInstance> waitRegisterList = new LinkedList<AntClientInstance>();
	private Thread registerCheckThread;
	private AntRegisterCheckService registerCheckService;
	private AntRuntimeService runtimeService;
	public AntClientRegisterService(AntRuntimeService runtimeService) {
		this.runtimeService = runtimeService;
		registerCheckService = new AntRegisterCheckService(this);
		registerCheckThread = new Thread(registerCheckService);
		registerCheckThread.setName("Ant-Regist-Check-Server"+runtimeService.getContextConfigure().getName());
		registerCheckThread.setDaemon(true);
		registerCheckThread.start();
	}
	public LinkedList<AntClientInstance> getWaitRegisterList() {
		return waitRegisterList;
	}
	
	public void check() {
		//获取当前时间
		long now = System.currentTimeMillis();
		//锁定列表
		synchronized (waitRegisterList) {
			//迭代
			Iterator<AntClientInstance> iterator = waitRegisterList.iterator();
			while(iterator.hasNext()) {
				AntClientInstance handler = iterator.next();
				//注册时间判断
				if(now - handler.getServiceInstance().getClientTime()>
				handler.getRuntimeService().getContextConfigure().getTimeout()) {
					//如果超时，移除并通知handler
					handler.registTimeout();
					iterator.remove();
				}
			}
		}
	}
	
	
	public static class AntRegisterCheckService implements Runnable{
		private volatile boolean available;
		private AntClientRegisterService registerService;
		public AntRegisterCheckService(AntClientRegisterService registerService) {
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


	public void register(AntClientInstance handler) {
		if(!this.waitRegisterList.contains(handler))
			this.waitRegisterList.add(handler);
	}
	public void registerSuccess(AntClientInstance handler) {
		this.waitRegisterList.remove(handler);
	}
}