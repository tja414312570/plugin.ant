package com.yanan.framework.a.proxy;

import com.yanan.framework.a.channel.socket.LockSupports;

public class Callback<T> {
	Callback(){}
	private Failed<Object> failed;
	private Success<Object> success;
	public void on(Success<Object> success, Failed<Object> failed) {
		this.success = success;
		this.failed = failed;
	}
	public Success<Object> success() {
		return success;
	}

	public Failed<Object> failed() {
		return failed;
	}
	public static <T> Callback<T> newCallback(Object instance) {
		Callback<T> callback = new Callback<T>();
		System.err.println(instance);
		LockSupports.set(instance, Callback.class, callback);
		Callback<Object> callBack = LockSupports.get(instance, Callback.class);
		System.err.println("回调:"+callBack);
		return callback;
	}
	public void wrapper(Object multi, Success<Object> success, Failed<Object> failed) {
		on(success,failed);
	}
}
