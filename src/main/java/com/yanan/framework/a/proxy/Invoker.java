package com.yanan.framework.a.proxy;

import com.yanan.framework.a.dispatcher.DispatcherContext;

public interface Invoker<K extends DispatcherContext<?>> {
	public void execute(K dispatcherContext);
}
