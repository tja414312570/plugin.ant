package com.yanan.framework.ant.core.cluster;

import java.util.List;
import java.util.Map;

import com.yanan.framework.ant.core.MessageChannel;
import com.yanan.framework.ant.core.server.ServerMessageChannel;
import com.yanan.framework.plugin.annotations.Service;

/**
 * 通道管理
 * @author yanan
 *
 */
@Service 
public interface ChannelManager<K> {
	
	/**
	 * 注册通道
	 * @param name 通道名字
	 * @param channel 服务通道
	 */
	void registerChannel(K name,ServerMessageChannel<?> channel);
	
	/**
	 * 根据通道名获取通道
	 * @param <T> 通道传输的数据类型
	 * @param name 通道名
	 * @return 通道
	 */
	<T> MessageChannel<T> getChannel(K name);
	
	/**
	 * 获取通道列表
	 * @param <T> 
	 * @param name
	 * @return
	 */
	<T> List<MessageChannel<T>> getChannelList(K name);
	
	/**
	 * 获取所有通道
	 * @param <T>
	 * @return
	 */
	<T> Map<String,List<MessageChannel<T>>> getAllChannel();
	
	/**
	 * 启动通道管理，并开启服务通道
	 * @param serverMessageChannel
	 */
	void start(ServerMessageChannel<?> serverMessageChannel);
	
	/**
	 * 启动通道管理
	 */
	void start();
	
	/**
	 * 关闭通道
	 */
	void close();
	
	/**
	 * 获取服务通道
	 * @param <T>
	 * @return
	 */
	<T> ServerMessageChannel<T> getServerChannel();
}