package com.yanan.framework.ant.core;

public interface AbstractMessageChannelHandler<T> {
	
	void handleRead(T value);
	
}
