package com.yanan.framework.ant.channel.socket.message;

/**
 * 私有消息协议
 * @author YaNan
 *
 * @param <T>
 */
public interface Message<T> {
	int getId();
	T getMessageType();
	<K> K getMessage();
}
