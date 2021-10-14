package com.yanan.framework.ant.core;

/**
 * 消息转化和序列化工具
 * @author yanan
 *
 * @param <T>
 */
public interface MessageHandler<T> {
	void onMessage(T message);
}
