package com.yanan.framework.a.dispatcher;

import com.yanan.framework.a.core.cluster.ChannelManager;
import com.yanan.framework.ant.model.AntRequest;

public interface ChannelDispatcher {

	void bind(ChannelManager<?> server);

	Object request(AntRequest request);

}
