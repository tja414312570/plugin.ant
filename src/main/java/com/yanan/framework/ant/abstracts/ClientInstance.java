package com.yanan.framework.ant.abstracts;


import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.yanan.framework.ant.handler.AntServiceInstance;
import com.yanan.framework.ant.interfaces.AntMessageSerialization;
import com.yanan.framework.ant.model.AntMessagePrototype;
import com.yanan.framework.ant.model.AntProviderSummary;
import com.yanan.framework.ant.service.AntRuntimeService;

/**
 * 连接实例，将连接层抽象出来以实现多种协议
 * @author yanan
 */
public interface ClientInstance {
	/**
	 * 绑定读
	 * @param key
	 */
	void handleRead(SelectionKey key);
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
	 * 初始化数据
	 * @param clientService
	 * @param socketChannel
	 * @param antProviderSummary
	 */
	void init(ClientService clientService, SocketChannel socketChannel, AntProviderSummary antProviderSummary);
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