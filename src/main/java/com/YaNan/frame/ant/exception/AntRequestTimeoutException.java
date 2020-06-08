package com.YaNan.frame.ant.exception;

import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.model.AntMessagePrototype;

public class AntRequestTimeoutException extends AntMessageException {

	public AntRequestTimeoutException(String msg) {
		super(msg);
	}
	public AntRequestTimeoutException(String msg, AntMessagePrototype message) {
		super(msg,message,AntClientHandler.getHandler());
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -7261131503079279781L;

}
