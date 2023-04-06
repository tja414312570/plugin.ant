package com.yanan.framework.ant.core;

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
	public Object around(MethodHandler methodHandler) throws Throwable {
		InterestedEventSource channelEven = ChannelEventManger.getInstance().getEventSource(methodHandler.getPlugsProxy().getProxyObject());
		if(channelEven != null) {
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
		try {
			Object result = methodHandler.invoke();
			channelEven = ChannelEventManger.getInstance().getEventSource(methodHandler.getPlugsProxy().getProxyObject());
			if(channelEven != null) {
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
			return result;
		}catch(Exception e) {
			channelEven = ChannelEventManger.getInstance().getEventSource(methodHandler.getPlugsProxy().getProxyObject());
			if(channelEven != null) {
				Environment.getEnviroment().distributeEvent(channelEven, ChannelEvent.EXCEPTION);
			}
			throw e;
		}
	}
}
