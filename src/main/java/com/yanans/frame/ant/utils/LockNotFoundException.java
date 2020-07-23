package com.yanan.frame.ant.utils;

public class LockNotFoundException extends RuntimeException {

	public LockNotFoundException(Object lockObject) {
		super("could not found lock for "+lockObject);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8019615893196330187L;

}