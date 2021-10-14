package com.yanan.framework.ant.channel.socket;

import java.nio.channels.SocketChannel;

/**
 * socket消息通道处理
 * @author tja41
 *
 */
public interface SocketMessageChannelHandler {
	void setSocketChannel(SocketChannel socketChannel);
}
