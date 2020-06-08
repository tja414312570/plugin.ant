package com.YaNan.frame.ant.utils;

public class LockTimeoutException extends RuntimeException {

	public LockTimeoutException(Object object,long time,boolean type) {
		super("failed to "+(type?"get":"release")+" lock for "+object +" at "+time +"s");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8211485007852036110L;

}
