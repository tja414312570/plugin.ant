package com.yanan.framework.ant.core.server;

import java.net.URI;
import java.util.List;

import com.yanan.framework.ant.channel.socket.server.MessageChannelCreateListener;
import com.yanan.framework.ant.core.MessageChannel;

/**
 * 服务通道
 * @author YaNan
 *
 */
public interface ServerMessageChannel<T> {
	URI getServerAddr();
	/**
	 * 获取MessageChannel
	 * @return
	 */
	MessageChannel<T> getMessageChannel(String channelMark);
	/**
	 * 获取MessageChannel
	 * @return
	 */
	List<MessageChannel<T>> getMessageChannelList();
	/**
	 * 打开通道
	 */
	void open();
	/**
	 * 关闭通道
	 */
	void close();
	/**
	 * 创建通道监听
	 */
	void onChannelCreate(MessageChannelCreateListener<T> message);
	
	/**
	 * 获取服务通道
	 * @param <K>
	 * @return
	 */
	<K> K getServerMessageChannel();
}
