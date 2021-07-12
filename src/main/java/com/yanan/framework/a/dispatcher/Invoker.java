package com.yanan.framework.a.dispatcher;

public interface Invoker<T> {
	public void bind(ChannelDispatcher<T> channelDispatcher);
}
