package com.yanan.framework.a.core.cluster;

import com.yanan.framework.a.core.MessageChannel;

public interface ChannelCreator<T,K> {
	MessageChannel<T> creatorChannel(K info);
}
