package com.yanan.framework.ant.core.server;

import com.yanan.framework.ant.core.MessageChannel;

/**
 * 
 * @author YaNan
 * @param <T>
 */
public interface ServerMessageChannelLinstener {
	void onChannelCreate(MessageChannel<?> messageChannel);
}
