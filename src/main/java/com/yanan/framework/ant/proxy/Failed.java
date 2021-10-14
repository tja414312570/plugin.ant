package com.yanan.framework.ant.proxy;

import com.yanan.framework.ant.dispatcher.DefaultDispatcherContext;
import com.yanan.framework.ant.dispatcher.DispatcherContext;

public interface Failed<T> {
	void onException(Exception exception,DispatcherContext<T> dispatcherContext);
	@SuppressWarnings("unchecked")
	default void onException(Exception exception, DefaultDispatcherContext<Object> dispatcherContext) {
		this.onException(exception, (DispatcherContext<T>)dispatcherContext);
	}
}
