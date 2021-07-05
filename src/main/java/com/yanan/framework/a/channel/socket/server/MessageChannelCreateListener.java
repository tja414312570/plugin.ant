package com.yanan.framework.a.channel.socket.server;

import com.yanan.framework.a.core.MessageChannel;

public interface MessageChannelCreateListener<T> {
	void onCreate(MessageChannel<T> messageChannel);
}
