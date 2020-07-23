package com.yanan.frame.ant.exception;

public class AntMessageResolveException extends RuntimeException {

	private int RID;

	public AntMessageResolveException(String msg) {
		super(msg);
	}

	public AntMessageResolveException(String msg, int rID) {
		super(msg);
		this.setRID(rID);
	}

	public int getRID() {
		return RID;
	}

	public void setRID(int rID) {
		RID = rID;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7631904068238649856L;

}