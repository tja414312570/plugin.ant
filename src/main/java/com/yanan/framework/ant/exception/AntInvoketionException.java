package com.yanan.framework.ant.exception;

import com.yanan.framework.ant.model.AntMessagePrototype;
import com.yanan.framework.ant.type.MessageType;

public class AntInvoketionException extends AntMessagePrototype {
	/**
	 * Invoke Exception
	 * @param serviceName
	 * @param exception
	 * @param i
	 * @param j
	 */
	public AntInvoketionException(String exception,int rid) {
		this.setInvokeParmeters("Ant Service Invoketion exception at service name,exception info:"+exception);
		this.type = MessageType.EXCEPTION;
		this.setRID(rid);
	}
}