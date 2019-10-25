package com.YaNan.frame.ant.service;

import java.util.Iterator;
import java.util.LinkedList;

import com.YaNan.frame.ant.AntContext;
import com.YaNan.frame.ant.handler.AntClientHandler;

public class AntClientRegisterService {
	private static AntClientRegisterService antClientRegisterService;
	private LinkedList<AntClientHandler> waitRegisterList = new LinkedList<AntClientHandler>();
	private Thread registerCheckThread;
	private AntRegisterCheckService registerCheckService;
	private AntClientRegisterService() {
		registerCheckService = new AntRegisterCheckService(this);
		registerCheckThread = new Thread(registerCheckService);
		registerCheckThread.setName("Ant-Regist-Check-Server");
		registerCheckThread.setDaemon(true);
		registerCheckThread.start();
	}
	public static AntClientRegisterService getInstance() {
		if(antClientRegisterService == null) {
			synchronized (AntClientRegisterService.class) {
				if(antClientRegisterService == null) {
					antClientRegisterService = new AntClientRegisterService();
				}
			}
		}
		return antClientRegisterService;
	}
	public LinkedList<AntClientHandler> getWaitRegisterList() {
		return waitRegisterList;
	}
	
	public void check() {
		//获取当前时间
		long now = System.currentTimeMillis();
		//锁定列表
		synchronized (waitRegisterList) {
			//迭代
			Iterator<AntClientHandler> iterator = waitRegisterList.iterator();
			while(iterator.hasNext()) {
				AntClientHandler handler = iterator.next();
				//注册时间判断
				if(now - handler.getClientTime()>
				AntContext.getContext().getContextConfigure().getTimeout()) {
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
					Thread.sleep(AntContext.getContext().getContextConfigure().getCheckTime());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		public void close() {
			available = false;
		}
		
	}


	public void register(AntClientHandler handler) {
		this.waitRegisterList.add(handler);
	}
	public void registerSuccess(AntClientHandler handler) {
		this.waitRegisterList.remove(handler);
	}
}
