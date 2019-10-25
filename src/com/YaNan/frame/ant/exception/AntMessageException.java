package com.YaNan.frame.ant.exception;

import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.model.AntMessagePrototype;

public class AntMessageException extends RuntimeException{
	private AntMessagePrototype antMessage;
	private AntClientHandler handler;
	public AntMessageException(AntMessagePrototype antMessage, AntClientHandler handler) {
		super();
		this.antMessage = antMessage;
		this.handler = handler;
	}
	public AntMessageException(String msg,AntMessagePrototype antMessage, AntClientHandler handler) {
		super(msg);
		this.antMessage = antMessage;
		this.handler = handler;
	}
	public AntMessageException(String msg,Throwable e,AntMessagePrototype antMessage, AntClientHandler handler) {
		super(msg,e);
		this.antMessage = antMessage;
		this.handler = handler;
	}
	public AntMessageException(Throwable e,AntMessagePrototype antMessage, AntClientHandler handler) {
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
	public AntClientHandler getHandler() {
		return handler;
	}
	private static final long serialVersionUID = -661004667125457130L;
}
