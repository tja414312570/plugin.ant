package com.yanan.framework.a.core.server;

import com.yanan.framework.plugin.event.InterestedEventSource;

public class ServerChannelEventSource extends InterestedEventSource{

	private ServerMessageChannel messageChannel;

	public ServerChannelEventSource(ServerMessageChannel messageChannel) {
		this.messageChannel = messageChannel;
	}

	public ServerMessageChannel getChannel() {
		return messageChannel;
	}
	
}
