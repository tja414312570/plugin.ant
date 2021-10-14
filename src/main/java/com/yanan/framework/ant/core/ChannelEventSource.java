package com.yanan.framework.ant.core;

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
