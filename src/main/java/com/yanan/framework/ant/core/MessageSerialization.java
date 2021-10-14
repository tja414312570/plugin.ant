package com.yanan.framework.ant.core;

import java.nio.ByteBuffer;

/**
 * 消息序列化接口
 * @author yanan
 */
public interface MessageSerialization {
	/**
	 * 序列化
	 * @param serailBean
	 * @return
	 */
	ByteBuffer serial(Object serailBean);
	/**
	 * 反序列化
	 * @param <T>
	 * @param byteBuffer
	 * @param position
	 * @param limit
	 * @param type
	 * @return
	 */
	<T> T deserial(ByteBuffer byteBuffer, int position, int limit, Class<T> type);
}
