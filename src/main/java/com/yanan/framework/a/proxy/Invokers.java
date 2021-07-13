package com.yanan.framework.a.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;

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
	public Class<?> getInvokeClass() {
		return invokeClass;
	}
	public void setInvokeClass(Class<?> invokeClass) {
		this.invokeClass = invokeClass;
	}
	public Method getInvokeMethod() {
		return invokeMethod;
	}
	public void setInvokeMethod(Method invokeMethod) {
		this.invokeMethod = invokeMethod;
	}
	public Object[] getInvokeParmeters() {
		return invokeParmeters;
	}
	public void setInvokeParmeters(Object[] invokeParmeters) {
		this.invokeParmeters = invokeParmeters;
	}
	@Override
	public String toString() {
		return "Invokers [invokeClass=" + invokeClass + ", invokeMethod=" + invokeMethod + ", invokeParmeters="
				+ Arrays.toString(invokeParmeters) + "]";
	}
}
