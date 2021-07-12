package com.yanan.framework.a.dispatcher;

import com.yanan.framework.a.core.cluster.ChannelManager;

public interface ChannelDispatcher<K> {

	void bind(ChannelManager<?> server);

	Object request(K channel,Object request);

	void bind(Invoker<?> invoker);

}
