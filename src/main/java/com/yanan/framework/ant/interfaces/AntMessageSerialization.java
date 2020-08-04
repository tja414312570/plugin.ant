package com.yanan.framework.ant.interfaces;

import java.nio.ByteBuffer;

import com.yanan.framework.ant.model.AntMessagePrototype;

/**
 * 消息序列化接口
 * @author yanan
 *
 */
public interface AntMessageSerialization {
	/**
	 * 将对象序列化
	 * @param serailBean 需要序列化的对象
	 * @param message 序列化对象所在的消息对象
	 * @return 序列化后的buffer
	 */
	ByteBuffer serialAntMessage(Object serailBean,AntMessagePrototype message);
	/**
	 * 将对象反序列化
	 * @param byteBuffer 资源缓存
	 * @param position 资源开始位
	 * @param limit 资源限制位
	 * @param type 反序列化对象类型
	 * @return 反序列化后的对象
	 */
	<T> T deserializationAntMessage(ByteBuffer byteBuffer, int position, int limit, Class<T> type);
	/**
	 * 清理缓冲
	 */
	void clear();
}