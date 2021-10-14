package com.yanan.framework.ant.dispatcher;

import com.yanan.framework.ant.core.cluster.ChannelManager;
import com.yanan.framework.ant.proxy.Callback;
import com.yanan.framework.ant.proxy.Invoker;

/**
 * 通道调配器
 * 用于通道之间数据调配
 * @author tja41
 *
 */
public interface ChannelDispatcher {

	/**
	 * 绑定通道管理
	 * @param <K>
	 * @param server
	 */
	<K> void bind(ChannelManager<K> server);

	/**
	 * 请求数据
	 * @param <K>
	 * @param channel
	 * @param request
	 * @return
	 */
	<K> Object request(K channel,Object request);
	
	/**
	 * 异步请求
	 * @param <K>
	 * @param channel
	 * @param request
	 * @param callBack
	 */
	<K> void requestAsync(K channel,Object request,Callback<?> callBack);

	/**
	 * 绑定数据
	 * @param invoker
	 */
	void bind(Invoker<?> invoker);

}
