package com.yanan.framework.a.proxy;

import com.yanan.framework.a.dispatcher.DispatcherContext;

public interface Success<T> {
	void onMessage(T result,DispatcherContext<T> dispatcherContext);
}
