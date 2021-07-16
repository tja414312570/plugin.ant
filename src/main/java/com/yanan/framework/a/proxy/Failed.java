package com.yanan.framework.a.proxy;

import com.yanan.framework.a.dispatcher.DispatcherContext;

public interface Failed<T> {
	void onException(Exception exception,DispatcherContext<T> dispatcherContext);
}
