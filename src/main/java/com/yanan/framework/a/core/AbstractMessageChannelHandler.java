package com.yanan.framework.a.core;

public interface AbstractMessageChannelHandler<T> {
	
	void handleRead(T value);
	
}
