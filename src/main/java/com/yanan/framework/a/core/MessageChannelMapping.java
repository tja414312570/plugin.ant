package com.yanan.framework.a.core;

import java.util.concurrent.ConcurrentHashMap;

import com.yanan.framework.plugin.annotations.Register;

@Register
public class MessageChannelMapping<T> extends ConcurrentHashMap<String,MessageChannel<T>>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6535337394800356157L;

}
