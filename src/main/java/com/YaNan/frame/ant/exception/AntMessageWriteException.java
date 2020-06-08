package com.YaNan.frame.ant.exception;

import java.io.WriteAbortedException;

import com.YaNan.frame.ant.model.AntMessagePrototype;

public class AntMessageWriteException extends RuntimeException {

	private AntMessagePrototype errorMessage;

	public AntMessageWriteException(WriteAbortedException e, AntMessagePrototype message) {
		super(e);
		this.setErrorMessage(message);
	}

	public AntMessagePrototype getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(AntMessagePrototype errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7082935944152704407L;

}
