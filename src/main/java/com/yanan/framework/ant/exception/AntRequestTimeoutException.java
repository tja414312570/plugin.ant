package com.yanan.framework.ant.exception;

import com.yanan.framework.ant.handler.AntServiceInstance;
import com.yanan.framework.ant.model.AntMessagePrototype;

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