package com.YaNan.frame.ant.protocol.ant.handler;

import com.YaNan.frame.ant.abstracts.AbstractProcess;
import com.YaNan.frame.ant.protocol.ant.AntClientInstance;

/**
 * 抽象接口注册服务
 * @author yanan
 *
 */
public abstract class AntAbstractRegisterHandler extends AbstractProcess{
	/**
	 * 需要注册的实例
	 */
	protected AntClientInstance clientInstance;
	/**
	 * 注册的方法的实现
	 */
	public abstract void regist();
	/**
	 * 默认构造器
	 * @param handler
	 */
	public AntAbstractRegisterHandler(AntClientInstance clientInstance) {
		this.clientInstance = clientInstance;
	}
	public AntClientInstance getClientInstance() {
		return clientInstance;
	}
	@Override
	public void execute() {
		this.regist();
	}
}
