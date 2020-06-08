package com.YaNan.frame.ant.abstracts;

import com.YaNan.frame.ant.handler.AntClientHandler;

/**
 * 抽象接口注册服务
 * @author yanan
 *
 */
public abstract class AntAbstractRegisterHandler extends AbstractProcess{
	/**
	 * 需要注册的handler
	 */
	protected AntClientHandler clientHandler;
	/**
	 * 注册的方法的实现
	 */
	public abstract void regist();
	/**
	 * 默认构造器
	 * @param handler
	 */
	public AntAbstractRegisterHandler(AntClientHandler handler) {
		this.clientHandler = handler;
	}
	public AntClientHandler getClientHandler() {
		return clientHandler;
	}
	public void setClientHandler(AntClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}
	@Override
	public void execute() {
		this.regist();
	}
}
