package com.yanan.framework.a.core.server;

import com.yanan.framework.a.core.MessageChannel;

/**
 * 
 * @author YaNan
 * @param <T>
 */
public interface ServerMessageChannelLinstener {
	void onChannelCreate(MessageChannel<?> messageChannel);
}
