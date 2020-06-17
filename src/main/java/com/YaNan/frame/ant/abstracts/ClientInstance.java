package com.YaNan.frame.ant.abstracts;


import com.YaNan.frame.ant.handler.AntServiceInstance;
import com.YaNan.frame.ant.interfaces.AntMessageSerialization;
import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.service.AntRuntimeService;

/**
 * 连接实例，将连接层抽象出来以实现多种协议
 * @author yanan
 */
public interface ClientInstance {
	/**
	 * 写入消息
	 * @param message
	 */
	void write(AntMessagePrototype message);
	/**
	 * 关闭链接
	 * @param cause
	 */
	void close(Throwable cause) ;
	/**
	 * 获取服务
	 * @return
	 */
	AntServiceInstance getServiceInstance();
	/**
	 * 获取地址
	 * @return
	 */
	String getRemoteAddress();
	/**
	 * 获取消息序列化工具
	 * @return
	 */
	AntMessageSerialization getSerailzationHandler();
	/**
	 * 获取运行时
	 * @return
	 */
	AntRuntimeService getRuntimeService();

}
