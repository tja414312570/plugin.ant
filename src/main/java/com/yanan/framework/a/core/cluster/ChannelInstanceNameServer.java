package com.yanan.framework.a.core.cluster;

public interface ChannelInstanceNameServer<T,K> {
	T getInstanceName(K name);
}
