package com.yanan.framework.a.channel.socket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

public class LockSupports {
	private static Map<Object,Thread> lockMap = new ConcurrentHashMap<>();
	public static void lock(Object lock) {
		Thread thread = Thread.currentThread();
		lockMap.put(lock, thread);
		LockSupport.park();
	}
	public static void unLock(Object lock) {
		Thread thread = lockMap.get(lock);
		if(thread != null)
			LockSupport.unpark(thread);
	}
}
