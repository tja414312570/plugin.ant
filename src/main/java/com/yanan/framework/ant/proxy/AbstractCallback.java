package com.yanan.framework.ant.proxy;

public class AbstractCallback extends Callback<Object>{

	private Failed<Object> failed;
	private Success<Object> success;

	@Override
	public void on(Success<Object> success, Failed<Object> failed) {
		this.success = success;
		this.failed = failed;
	}

	@Override
	public Success<Object> success() {
		return success;
	}

	@Override
	public Failed<Object> failed() {
		return failed;
	}
}
