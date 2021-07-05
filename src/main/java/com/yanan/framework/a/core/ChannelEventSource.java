package com.yanan.framework.a.core;

import com.yanan.framework.plugin.event.InterestedEventSource;

public class ChannelEventSource extends InterestedEventSource{

	private MessageChannel<?> messageChannel;

	public ChannelEventSource(MessageChannel<?> messageChannel) {
		this.messageChannel = messageChannel;
	}

	public MessageChannel<?> getMessageChannel() {
		return messageChannel;
	}
	
}
