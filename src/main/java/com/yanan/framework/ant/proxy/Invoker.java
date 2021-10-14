package com.yanan.framework.ant.proxy;

import com.yanan.framework.ant.dispatcher.DispatcherContext;

public interface Invoker<K extends DispatcherContext<?>> {
	public void execute(K dispatcherContext);
}
