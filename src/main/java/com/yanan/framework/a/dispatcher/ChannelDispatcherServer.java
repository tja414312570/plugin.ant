package com.yanan.framework.a.dispatcher;

import com.yanan.framework.a.core.cluster.ChannelManager;
import com.yanan.framework.plugin.annotations.Register;

@Register
public class ChannelDispatcherServer implements ChannelDispatcher{

	private ChannelManager<?> channelManager;

	@Override
	public void bind(ChannelManager<?> server) {
		this.channelManager = server;
	}

}
