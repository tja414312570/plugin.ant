package com.yanan.framework.a.channel.socket;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import com.yanan.framework.plugin.annotations.Register;

@Register
public class SocketChannelMappingManager<T> extends ConcurrentHashMap<SocketChannel, T>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2842066333646990798L;
	public void setMapping(SocketChannel socketChannel,T instance) {
		this.put(socketChannel, instance);
	}
	public T getMapping(SocketChannel socketChannel) {
		return this.get(socketChannel);
	}

}
