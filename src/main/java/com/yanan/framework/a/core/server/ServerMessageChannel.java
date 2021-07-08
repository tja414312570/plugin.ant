package com.yanan.framework.a.core.server;

import java.util.List;

import com.yanan.framework.a.channel.socket.server.MessageChannelCreateListener;
import com.yanan.framework.a.core.MessageChannel;

/**
 * 服务通道
 * @author YaNan
 *
 */
public interface ServerMessageChannel<T> {
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
	 * 
	 */
	void onChannelCreate(MessageChannelCreateListener<T> message);
	
	<K> K getServerMessageChannel();
}
