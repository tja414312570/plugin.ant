package com.yanan.frame.ant.model;

public class RegisterResult {
	/**
	 * 返回状态
	 */
	private int status;
	/**
	 * 返回id
	 */
	private int sid;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	@Override
	public String toString() {
		return "RegisterResult [status=" + status + ", sid=" + sid + "]";
	}
}