package com.yanan.framework.ant.proxy;

import com.yanan.framework.ant.dispatcher.DispatcherContext;

/**
 * 订阅
 * @author tja41
 *
 * @param <T>
 */
public interface Subscribe<T> extends Callback<T>{
	void onNotify(DispatcherContext<T> ctx,T message);
	default void onError(DispatcherContext<T> ctx,Exception error) {};
	default void onMessage(DispatcherContext<T> ctx,T message) {this.onNotify(ctx, message);};
}
