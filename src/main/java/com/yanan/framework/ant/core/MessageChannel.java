package com.yanan.framework.ant.core;

/**
 * 消息通道，提供消息传输的通道
 * 仅用于传输数据，不处理消息序列化等
 * @author yanan
 */
public interface MessageChannel<T> {
	/**
	 * 打开通道
	 */
	void open();
	/**
	 * 关闭通道
	 */
	void close();
	/**
	 * 通道消息传输
	 */
	void transport(T message);
	/**
	 * 通道消息接收
	 */
	void accept(MessageHandler<T> message);
	/**
	 * 消息监听
	 * @param listener
	 */
	void setListener(MessageChannelListener<T> listener);
}
