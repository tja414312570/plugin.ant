package com.yanan.framework.ant.channel.socket;

import java.net.SocketOption;

public abstract class SocketOptions<T> implements SocketOption<T>{
	public final static SocketOption<Exception> EXCEPTION_OPTION = new SocketOptions<Exception>() {};
	@Override
	public String name() {
		return this.getClass().getName();
	}
	@SuppressWarnings("unchecked")
	@Override
	public Class<T> type() {
		return (Class<T>) this.getClass();
	}
}
