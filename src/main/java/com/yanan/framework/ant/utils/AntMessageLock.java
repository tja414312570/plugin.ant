package com.yanan.framework.ant.utils;

import com.yanan.framework.ant.model.AntMessagePrototype;

public class AntMessageLock {
	private long times; 
	private volatile AntMessagePrototype message;
	private volatile Object result;
	private volatile Throwable exception;
	private volatile boolean locked = true;
	public long getTimes() {
		return times;
	}


	@Override
	public String toString() {
		return "Lock [times=" + times + ", message=" + message + ", result=" + result + ", exception=" + exception
				+ ", locked=" + locked + "]";
	}


	public AntMessagePrototype getMessage() {
		return message;
	}

	public AntMessageLock(AntMessagePrototype message) {
		this.message = message;
		times = System.currentTimeMillis();
	}

	public void lock() {
		synchronized (this) {
			try {
				if(locked)
					this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void unLock() {
		synchronized (this) {
			locked = false;
			this.notifyAll();
		}
	}

	public static AntMessageLock getLock(AntMessagePrototype message) {
		return new AntMessageLock(message);
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public boolean isLocked() {
		return locked;
	}
}