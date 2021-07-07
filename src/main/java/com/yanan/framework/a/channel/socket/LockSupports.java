package com.yanan.framework.a.channel.socket;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

import com.yanan.utils.asserts.Assert;
import com.yanan.utils.reflect.ReflectUtils;
import com.yanan.utils.reflect.cache.ClassHelper;

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
	public static void set(Object lock,Object Key,Object value) {
		Thread thread = lockMap.get(lock);
		Assert.isNotNull(thread);
		Field threadLocalField = ClassHelper.getClassHelper(Thread.class).getField("threadLocals");
		Assert.isNotNull(threadLocalField);
		try {
			ThreadLocal<Map<Object,Object>> threadLocal = ReflectUtils.getFieldValue(threadLocalField, threadLocalField);
			if(threadLocal == null) {
//				threadLocal = new Thread
				ReflectUtils.setFieldValue(threadLocalField, threadLocalField, value);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	public void get() {
//		ThreadLocalMap map = getMap(t);
//        if (map != null) {
//            ThreadLocalMap.Entry e = map.getEntry(this);
//            if (e != null) {
//                @SuppressWarnings("unchecked")
//                T result = (T)e.value;
//                return result;
//            }
//        }
//        return setInitialValue();
	}
}
