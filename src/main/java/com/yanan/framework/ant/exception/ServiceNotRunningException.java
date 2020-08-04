package com.yanan.framework.ant.exception;

import com.yanan.framework.ant.handler.AntServiceInstance;

public class ServiceNotRunningException extends RuntimeException {

	private AntServiceInstance clientHandler;

	public ServiceNotRunningException(Throwable e) {
		super(e);
	}

	public ServiceNotRunningException() {
	}

	public ServiceNotRunningException(Throwable e, AntServiceInstance handler) {
		super(e);
		this.setClientHandler(handler);
	}

	public ServiceNotRunningException(AntServiceInstance handler) {
		super(handler == null?"invoke handler is null":"failed to invoke ant client "+handler.getClientInstance().getRemoteAddress());
		this.setClientHandler(handler);
	}

	public AntServiceInstance getClientHandler() {
		return clientHandler;
	}

	public void setClientHandler(AntServiceInstance clientHandler) {
		this.clientHandler = clientHandler;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7780444059977614069L;

}