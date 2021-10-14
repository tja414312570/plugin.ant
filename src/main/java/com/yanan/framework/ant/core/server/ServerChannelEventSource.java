package com.yanan.framework.ant.core.server;

import com.yanan.framework.plugin.event.InterestedEventSource;

public class ServerChannelEventSource extends InterestedEventSource{

	private ServerMessageChannel<?> messageChannel;

	public ServerChannelEventSource(ServerMessageChannel<?> messageChannel) {
		this.messageChannel = messageChannel;
	}

	public ServerMessageChannel<?> getChannel() {
		return messageChannel;
	}
	
}
