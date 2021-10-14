package com.yanan.framework.ant.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.yanan.utils.reflect.cache.ClassHelper;

public class Invokers {
	/**
	 * 消息调用的类
	 */
	protected String invokeClass;
	/**
	 * 消息调用的方法
	 */
	protected String invokeMethod;
	/**
	 * 消息参数
	 */
	protected Object[] invokeParmeters;
	public Class<?> getInvokeClass() {
		try {
			return ClassHelper.getClassHelper(invokeClass).getCacheClass();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("class not found!",e);
		}
	}
	public void setInvokeClass(Class<?> invokeClass) {
		this.invokeClass = invokeClass.getName();
	}
	public Method getInvokeMethod() {
		Class<?> clzz = getInvokeClass();
		Method[] methods = clzz.getDeclaredMethods();
		for(Method method : methods) {
			if(method.toString().equals(invokeMethod))
				return method;
		}
		throw new RuntimeException("method "+invokeMethod+"not found!");
	}
	public void setInvokeMethod(Method invokeMethod) {
		this.invokeMethod = invokeMethod.toString();
	}
	public Object[] getInvokeParmeters() {
		return invokeParmeters;
	}
	public void setInvokeParmeters(Object... invokeParmeters) {
		this.invokeParmeters = invokeParmeters;
	}
	@Override
	public String toString() {
		return "Invokers [invokeClass=" + invokeClass + ", invokeMethod=" + invokeMethod + ", invokeParmeters="
				+ Arrays.toString(invokeParmeters) + "]";
	}
}
