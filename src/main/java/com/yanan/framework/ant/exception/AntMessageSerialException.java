package com.yanan.framework.ant.exception;

import com.yanan.framework.ant.model.AntMessagePrototype;

public class AntMessageSerialException extends RuntimeException {

	private AntMessagePrototype messageBody;

	public AntMessageSerialException(String msg, Exception exception, AntMessagePrototype message) {
		super(msg,exception);
		this.messageBody = message;
		
	}

	public AntMessageSerialException(String msg, AntMessagePrototype message) {
		super(msg);
		this.messageBody = message;
	}

	public AntMessagePrototype getMessageBody() {
		return messageBody;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3018234507010323206L;

}