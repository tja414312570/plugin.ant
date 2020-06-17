package com.YaNan.frame.ant.exception;

import com.YaNan.frame.ant.handler.AntServiceInstance;

public class AntRequestException extends RuntimeException {

	private AntServiceInstance handler;

	public AntRequestException(Throwable e, AntServiceInstance handler) {
		super(e);
		this.setHandler(handler);
	}

	public AntRequestException(String msg) {
		super(msg);
	}

	public AntServiceInstance getHandler() {
		return handler;
	}

	public void setHandler(AntServiceInstance handler) {
		this.handler = handler;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7657225306222829493L;

}
