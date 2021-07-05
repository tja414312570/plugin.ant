package com.yanan.framework.a.core;

/**
 * 消息通道，提供消息传输的通道
 * 仅用于传输数据，不处理消息序列化等
 * @author yanan
 */
public interface MessageChannelListener<T> {
	/**
	 * 打开通道
	 */
	void onOpen(MessageChannel<T> channel);
	/**
	 * 关闭通道
	 */
	void onClose(MessageChannel<T> channe);
	/**
	 * 通道消息传输
	 */
	void onTransport(MessageChannel<T> channe,T message);
	/**
	 * 通道消息接收
	 */
	void onAccept(MessageChannel<T> channe,T message);
	/**
	 * 当异常发生
	 * @param channe
	 * @param exception
	 */
	void onException(MessageChannel<T> channe,Exception exception);
}
