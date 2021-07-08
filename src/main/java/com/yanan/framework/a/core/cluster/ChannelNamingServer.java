package com.yanan.framework.a.core.cluster;

import com.yanan.framework.a.core.server.ServerMessageChannel;

public interface ChannelNamingServer<T> {
	T getServerName(ServerMessageChannel<?> messageChannel); 
}
