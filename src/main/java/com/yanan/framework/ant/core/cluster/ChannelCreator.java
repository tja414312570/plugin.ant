package com.yanan.framework.ant.core.cluster;

import com.yanan.framework.ant.core.MessageChannel;

public interface ChannelCreator<T,K> {
	MessageChannel<T> creatorChannel(K info);
}
