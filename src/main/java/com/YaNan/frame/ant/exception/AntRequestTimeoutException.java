package com.YaNan.frame.ant.exception;

import com.YaNan.frame.ant.handler.AntServiceInstance;
import com.YaNan.frame.ant.model.AntMessagePrototype;

public class AntRequestTimeoutException extends AntMessageException {

	public AntRequestTimeoutException(String msg) {
		super(msg);
	}
	public AntRequestTimeoutException(String msg, AntMessagePrototype message) {
		super(msg,message,AntServiceInstance.getServiceInstance());
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -7261131503079279781L;

}
