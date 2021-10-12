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
	
	<T> MessageChannel<T> getChannel(K name);
	
	<T> List<MessageChannel<T>> getChannelList(K name);
	
	<T> Map<String,List<MessageChannel<T>>> getAllChannel();
	
	void start(ServerMessageChannel<?> serverMessageChannel);
	
	void start();
	
	void close();
	
	<T> ServerMessageChannel<T> getServerChannel();
}