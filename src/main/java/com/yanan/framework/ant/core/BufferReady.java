package com.yanan.framework.ant.core;

import java.nio.ByteBuffer;

/**
 * buffer通信的接口，提供通道和ByteBufferChannel通信的交换机制
 * @author yanan
 *
 * @param <T>
 */
public interface BufferReady<T> {
	/**
	 * 通道写入
	 * @param buffer
	 */
	public void write(ByteBuffer buffer);
	/**
	 * 通道读取
	 * @param buffer
	 */
	public void handleRead(ByteBuffer buffer);
	/**
	 * 获取消息
	 * @param message
	 */
	public void onMessage(T message);
}