package com.yanan.framework.ant;

import java.util.Properties;

public class ProertiesWrapper extends Properties{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3304356752276642630L;
	public ProertiesWrapper(Properties defaults) {
       super(defaults);
    }
	public ProertiesWrapper() {
       super();
    }
	public int getInt(String key,int defaultValue) {
		String result = getProperty(key);
		if(result == null)
			return defaultValue;
		return Integer.valueOf(result);
	}
	public long getLong(String key,long defaultValue) {
		String result = getProperty(key);
		if(result == null)
			return defaultValue;
		return Long.valueOf(result);
	}
	public boolean getBoolean(String key,boolean defaultValue) {
		String result = getProperty(key);
		if(result == null)
			return defaultValue;
		return Boolean.valueOf(result);
	}
	public double getDouble(String key,double defaultValue) {
		String result = getProperty(key);
		if(result == null)
			return defaultValue;
		return Double.valueOf(result);
	}
}