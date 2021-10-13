package com.yanan.framework.a.serial;


public class MessageSerialException extends RuntimeException{
	private static final long serialVersionUID = 9020106173816784218L;
	public MessageSerialException() {
	}
	public MessageSerialException(String msg, Throwable e) {
		super(msg,e);
	}
	public MessageSerialException(String msg) {
		super(msg);
	}
}
