package com.yanan.framework.ant.exception;

public class AntRuntimeException extends RuntimeException {

	public AntRuntimeException(String msg) {
		super(msg);
	}
	public AntRuntimeException(String msg,Throwable t) {
		super(msg,t);
	}
	public AntRuntimeException(Throwable t) {
		super(t);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -1023574281467844506L;

}