package com.yanan.framework.ant.proxy;

import com.yanan.framework.ant.channel.socket.LockSupports;

public class Callback<T> {
	Callback(){}
	private Failed<T> failed;
	private Success<T> success;
	public void on(Success<T> success, Failed<T> failed) {
		this.success = success;
		this.failed = failed;
	}
	public Success<T> success() {
		return success;
	}

	public Failed<T> failed() {
		return failed;
	}
	public static <T> Callback<T> newCallback(Object instance) {
		Callback<T> callback = new Callback<T>();
		LockSupports.set(instance, Callback.class, callback);
		return callback;
	}
	public void wrapper(Object multi, Success<T> success, Failed<T> failed) {
		on(success,failed);
	}
}
