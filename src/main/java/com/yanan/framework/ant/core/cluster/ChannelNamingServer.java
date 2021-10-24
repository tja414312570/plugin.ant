package com.yanan.framework.ant.core.cluster;

import com.yanan.framework.ant.core.server.ServerMessageChannel;

public interface ChannelNamingServer<T,K extends ServerMessageChannel<?>> {
	T getServerName(K messageChannel); 
}
