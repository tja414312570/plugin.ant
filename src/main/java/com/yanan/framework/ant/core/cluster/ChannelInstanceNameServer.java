package com.yanan.framework.ant.core.cluster;

public interface ChannelInstanceNameServer<T,K> {
	T getInstanceName(K name);
}
