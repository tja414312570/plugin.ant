package com.yanan.framework.a.dispatcher;

import com.yanan.framework.a.core.cluster.ChannelManager;
import com.yanan.framework.a.proxy.Invoker;

public interface ChannelDispatcher<K> {

	void bind(ChannelManager<?> server);

	Object request(K channel,Object request);
	
	void requestAsync(K channel,Object request,Callback callBack);

	void bind(Invoker<?> invoker);

}
