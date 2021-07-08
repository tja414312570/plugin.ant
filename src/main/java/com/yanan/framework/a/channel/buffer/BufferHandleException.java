package com.yanan.framework.a.channel.buffer;

public class BufferHandleException extends RuntimeException {

	public BufferHandleException(String msg, Throwable e) {
		super(msg,e);
	}

	public BufferHandleException(Throwable e) {
		super(e);
	}

	public BufferHandleException(String msg) {
		super(msg);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7985486002108420194L;

}
