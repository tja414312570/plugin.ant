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
	 * 响应异常
	 * @param t
	 */
	void exception(Throwable t);
	/**
	 * 消息通道
	 * @return
	 */
	MessageChannel<?> getMessageChannel();
	
	/**
	 * 获取当前请求的上下文
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K> DispatcherContext<K> getCurrentContext() {
		return (DispatcherContext<K>) ChannelDispatcherServer.dispatcherContextLocal.get();
	}
}
