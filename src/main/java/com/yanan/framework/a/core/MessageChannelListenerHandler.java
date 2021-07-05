package com.yanan.framework.a.core;

/**
 * 消息通道，提供消息传输的通道
 * 仅用于传输数据，不处理消息序列化等
 * @author yanan
 */
public interface MessageChannelListenerHandler<T> {
	/**
	 * 通道消息接收
	 */
	void onEvent(MessageChannel<T> channel);
}
