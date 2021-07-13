package com.yanan.framework.a.dispatcher;

import com.yanan.framework.a.core.MessageChannel;

public interface DispatcherContext<T> {
	T getMessage();
	
	void response(Object message);
	
	MessageChannel<?> getMessageChannel();
	
}
