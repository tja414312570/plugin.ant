package com.yanan.framework.ant.core;

/**
 * 消息转化和序列化工具
 * @author yanan
 *
 * @param <T>
 */
public interface ByteBufferChannel<T> {
	/**
	 * 处理读数据
	 */
	void handleRead();
	/**
	 * 写数据
	 * @param message
	 * @param messageWriteHandler
	 */
	void write(T message);
	/**
	 * 设置buffe交换
	 * @param messageWriteHandler
	 */
	void setBufferReady(BufferReady<T> messageWriteHandler);
}
