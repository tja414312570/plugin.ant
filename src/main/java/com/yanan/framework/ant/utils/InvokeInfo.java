package com.yanan.framework.ant.utils;

import java.lang.reflect.Method;

public class InvokeInfo {
	private Class<?> invokeClass;
	private Method invokeMethod;
	public Class<?> getInvokeClass() {
		return invokeClass;
	}
	public Method getInvokeMethod() {
		return invokeMethod;
	}
	public InvokeInfo(Class<?> invokeClass, Method invokeMethod) {
		this.invokeClass = invokeClass;
		this.invokeMethod = invokeMethod;
	}
}