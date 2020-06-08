package com.YaNan.frame.ant.exception;

public class AntResponseException extends RuntimeException {
	public AntResponseException(String msg, ClassNotFoundException e) {
		super(msg,e);
	}

	public AntResponseException(Throwable e) {
		super(e);
	}

	public AntResponseException(String t) {
		super(t);
	}

	public AntResponseException(Object t) {
		super(t==null?"Unknow Exception":t.toString());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}
