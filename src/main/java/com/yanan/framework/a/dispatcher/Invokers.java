package com.yanan.framework.a.dispatcher;

import java.lang.reflect.Method;

public class Invokers {
	/**
	 * 消息调用的类
	 */
	protected Class<?> invokeClass;
	/**
	 * 消息调用的方法
	 */
	protected Method invokeMethod;
	/**
	 * 消息参数
	 */
	protected Object[] invokeParmeters;
}
