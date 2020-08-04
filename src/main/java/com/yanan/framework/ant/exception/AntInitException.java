package com.yanan.framework.ant.exception;


public class AntInitException extends RuntimeException {

	public AntInitException(Throwable e) {
		super(e);
	}

	public AntInitException(String msg) {
		super(msg);
	}

	public AntInitException(String string, Throwable e) {
		super(string,e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5312596119555926039L;

}