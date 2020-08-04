package com.yanan.framework.ant.utils;

import java.util.HashMap;
import java.util.Map;

public class InvokeInfoCache {
	private static Map<String,InvokeInfo> invokeInfo = new HashMap<String,InvokeInfo>();
	public static void addInvokeInfo(String key,InvokeInfo info){
		invokeInfo.put(key, info);
	}
	public static InvokeInfo getInvokeInfo(String key){
		return invokeInfo.get(key);
	}
}