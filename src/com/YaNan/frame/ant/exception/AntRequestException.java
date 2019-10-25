package com.YaNan.frame.ant.exception;

import com.YaNan.frame.ant.handler.AntClientHandler;

public class AntRequestException extends RuntimeException {

	private AntClientHandler handler;

	public AntRequestException(Throwable e, AntClientHandler handler) {
		super(e);
		this.setHandler(handler);
	}

	public AntRequestException(String msg) {
		super(msg);
	}

	public AntClientHandler getHandler() {
		return handler;
	}

	public void setHandler(AntClientHandler handler) {
		this.handler = handler;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7657225306222829493L;

}
