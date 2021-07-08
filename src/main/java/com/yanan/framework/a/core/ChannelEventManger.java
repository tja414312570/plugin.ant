package com.yanan.framework.a.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yanan.framework.plugin.event.InterestedEventSource;

public class ChannelEventManger {
	private Map<Object,InterestedEventSource> eventMap = new ConcurrentHashMap<>();
	private ChannelEventManger() {};
	private static class eventHandler{
		static private ChannelEventManger instance = new ChannelEventManger();
	}
	public static ChannelEventManger getInstance() {
		return eventHandler.instance;
	}
	@SuppressWarnings("unchecked")
	public <T extends InterestedEventSource> T putEventSource(Object object,T eventSource) {
		return (T) eventMap.put(object, eventSource);
	}
	@SuppressWarnings("unchecked")
	public <T extends InterestedEventSource> T getEventSource(Object object) {
		return (T) eventMap.get(object);
	}
}
