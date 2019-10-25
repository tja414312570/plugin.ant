package com.YaNan.frame.ant.exception;

import com.YaNan.frame.ant.handler.AntClientHandler;

public class ServiceNotRunningException extends RuntimeException {

	private AntClientHandler clientHandler;

	public ServiceNotRunningException(Throwable e) {
		super(e);
	}

	public ServiceNotRunningException() {
	}

	public ServiceNotRunningException(Throwable e, AntClientHandler handler) {
		super(e);
		this.setClientHandler(handler);
	}

	public ServiceNotRunningException(AntClientHandler handler) {
		super(handler == null?"invoke handler is null":"failed to invoke ant client "+handler.getRemoteAddress());
		this.setClientHandler(handler);
	}

	public AntClientHandler getClientHandler() {
		return clientHandler;
	}

	public void setClientHandler(AntClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7780444059977614069L;

}
