package com.yanan.framework.ant.exception;

import com.yanan.framework.ant.handler.AntServiceInstance;
import com.yanan.framework.ant.model.AntMessagePrototype;

public class AntMessageException extends RuntimeException{
	private AntMessagePrototype antMessage;
	private AntServiceInstance handler;
	public AntMessageException(AntMessagePrototype antMessage, AntServiceInstance handler) {
		super();
		this.antMessage = antMessage;
		this.handler = handler;
	}
	public AntMessageException(String msg,AntMessagePrototype antMessage, AntServiceInstance handler) {
		super(msg);
		this.antMessage = antMessage;
		this.handler = handler;
	}
	public AntMessageException(String msg,Throwable e,AntMessagePrototype antMessage, AntServiceInstance handler) {
		super(msg,e);
		this.antMessage = antMessage;
		this.handler = handler;
	}
	public AntMessageException(Throwable e,AntMessagePrototype antMessage, AntServiceInstance handler) {
		super(e);
		this.antMessage = antMessage;
		this.handler = handler;
	}
	public AntMessageException(String msg) {
		super(msg);
	}
	public AntMessagePrototype getAntMessage() {
		return antMessage;
	}
	public AntServiceInstance getHandler() {
		return handler;
	}
	private static final long serialVersionUID = -661004667125457130L;
}