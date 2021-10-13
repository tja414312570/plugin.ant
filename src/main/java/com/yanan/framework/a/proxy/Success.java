package com.yanan.framework.a.proxy;

import com.yanan.framework.a.dispatcher.DefaultDispatcherContext;
import com.yanan.framework.a.dispatcher.DispatcherContext;

public interface Success<T> {
	void onMessage(T result,DispatcherContext<T> dispatcherContext);

	@SuppressWarnings("unchecked")
	default void onMessage(Object invoker, DefaultDispatcherContext<Object> dispatcherContext) {
		this.onMessage((T)invoker, (DispatcherContext<T>)dispatcherContext);
	}
}
