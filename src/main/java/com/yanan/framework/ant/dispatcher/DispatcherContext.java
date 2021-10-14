package com.yanan.framework.ant.dispatcher;

import com.yanan.framework.ant.core.MessageChannel;

/**
 * 调配上下文
 * @author tja41
 *
 * @param <T>
 */
public interface DispatcherContext<T> {
	/**
	 * 获取消息
	 * @return
	 */
	T getMessage();
	/**
	 * 响应数据
	 * @param message
	 */
	void response(Object message);
	/**
	 * 消息通道
	 * @return
	 */
	MessageChannel<?> getMessageChannel();
	
}
