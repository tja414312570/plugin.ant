package com.yanan.framework.a.core.cluster;

import java.util.List;
import java.util.Map;

import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.server.ServerMessageChannel;
import com.yanan.framework.plugin.annotations.Service;

/**
 * 服务中心接口
 * @author yanan
 *
 */
@Service //表明需要被代理
public interface ChannelManager<K> {
	
	void registerChannel(K name,ServerMessageChannel<?> channel);
	
	<T,I> MessageChannel<T> getChannel(I name);
	
	<T,I> List<MessageChannel<T>> getChannelList(I name);
	
	<T,I> Map<String,List<MessageChannel<T>>> getAllChannel();
	
	void start(ServerMessageChannel<?> serverMessageChannel);
	
	void start();
	
	void close();
}