package com.yanan.framework.a.core;

import com.yanan.framework.plugin.Environment;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.event.InterestedEventSource;
import com.yanan.framework.plugin.handler.InvokeHandler;
import com.yanan.framework.plugin.handler.MethodHandler;

/**
 * 通道事件调配分发
 * @author yanan
 *
 */
@Register(attribute = {"com.yanan.framework.a.core.MessageChannel.*","com.yanan.framework.a.core.ServerMessageChannel.*"})
public class ChannelEventDispatcher implements InvokeHandler{

	@Override
	public void before(MethodHandler methodHandler) {
		InterestedEventSource channelEven = ChannelEventManger.getInstance().getEventSource(methodHandler.getPlugsProxy().getProxyObject());
		if(channelEven == null)
			return;
		switch(methodHandler.getMethod().getName()) {
		case "open":
			Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.OPENING);
			break;
		case "close":
			Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.CLOSE);
			break;
		case "transport":
			Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.WRITEING);
			break;
		case "accept":
			Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.READING);
			break;
		}
	}

	@Override
	public void after(MethodHandler methodHandler) {
		InterestedEventSource channelEven = ChannelEventManger.getInstance().getEventSource(methodHandler.getPlugsProxy().getProxyObject());
		if(channelEven == null)
			return;
		switch(methodHandler.getMethod().getName()) {
		case "open":
			Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.OPEN);
			break;
		case "close":
			Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.CLOSE);
			break;
		case "transport":
			Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.OPEN);
			break;
		case "accept":
			Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.OPEN);
			break;
		}
	}

	@Override
	public void error(MethodHandler methodHandler, Throwable exception) {
		System.err.println("调用异常:"+methodHandler.getMethod());
		InterestedEventSource channelEven = ChannelEventManger.getInstance().getEventSource(methodHandler.getPlugsProxy().getProxyObject());
		if(channelEven == null)
			return;
		Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.EXCEPTION);
	}
	
}
