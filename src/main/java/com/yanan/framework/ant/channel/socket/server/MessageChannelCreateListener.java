package com.yanan.framework.ant.channel.socket.server;

import com.yanan.framework.ant.core.MessageChannel;

/**
 * 消息通道创建监听
 * @author tja41
 *
 * @param <T>
 */
public interface MessageChannelCreateListener<T> {
	void onCreate(MessageChannel<T> messageChannel);
}
