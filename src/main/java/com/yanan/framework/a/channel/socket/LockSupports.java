package com.yanan.framework.a.channel.socket;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

import com.yanan.utils.CacheHashMap;
import com.yanan.utils.asserts.Assert;
import com.yanan.utils.reflect.ReflectUtils;
import com.yanan.utils.reflect.TypeToken;
import com.yanan.utils.reflect.cache.ClassHelper;

/**
 * 提供线程线程通信
 * @author YaNan
 *
 */
public class LockSupports {
	//使用缓存表以防内存泄漏
	static CacheHashMap<Object,Thread> lockMap = new CacheHashMap<>(new TypeToken<WeakReference<?>>() {}.getTypeClass());
	//ThreadLocal
	static ThreadLocal<Map<Object,Object>> threadLocal = new ThreadLocal<>();
	private static class UNLOCKTHROWS{}
	public static void lock(Object lock) {
		setLockThread(lock);
		LockSupport.park();
	}
	public static void lock(Object lock,long timeout) {
		setLockThread(lock);
		LockSupport.parkNanos(timeout);
	}
	public static void lockAndCatch(Object lock) {
		lock(lock);
		Exception e = get(lock,UNLOCKTHROWS.class);
		if(e != null)
			throw new LockThrowsException(e);
	}
	public static void lockAndCatch(Object lock,long timeout) {
		lock(lock,timeout);
		Exception e = get(lock,UNLOCKTHROWS.class);
		if(e != null)
			throw new LockThrowsException(e);
	}
	public static void setLockThread(Object lock) {
		Thread thread = Thread.currentThread();
		lockMap.puts(lock, thread);
	}
	public static void removeLockThread(Object lock) {
		lockMap.remove(lock);
	}
	public static void unLockAndThrows(Object lock,Exception exception) {
		set(lock,UNLOCKTHROWS.class,exception);
		unLock(lock);
	}
	public static Thread getLockThread(Object lock) {
		Thread threadLock = lockMap.get(lock);
		return threadLock;
	}
	public static void unLock(Object lock) {
		Thread thread =getLockThread(lock);
		if(thread != null) 
			LockSupport.unpark(thread);
	}
	public static void set(Object lock,Object key,Object value) {
		Thread thread = getLockThread(lock);
		if(thread == null)
			setLockThread(lock);
		Map<Object,Object> valueMap = getThreadLocalValue(thread, threadLocal);
		if(valueMap ==null)
			valueMap = createThreadLocalMap(thread,threadLocal, new ConcurrentHashMap<>());
		valueMap.put(key, value);
	}
	@SuppressWarnings("unchecked")
	public static <T> T get(Object lock,Object key) {
		Thread thread = getLockThread(lock);
		Assert.isNotNull(thread,"thread is null");
		Map<Object,Object> valueMap = getThreadLocalValue(thread, threadLocal);
		if(valueMap !=null) {
			return (T) valueMap.get(key);
		}
		return null;
	}
	/**
	 * 參考ThreadLocal的get()方法
	 * @param <T>
	 * @param thread
	 * @param threadLocal
	 * @return
	 */
	public static <T> T getThreadLocalValue(Thread thread,ThreadLocal<T> threadLocal) {
		try {
			Field threadLocalField = ClassHelper.getClassHelper(Thread.class).getDeclaredField("threadLocals");
			Assert.isNotNull(threadLocalField,"field [threadLocals] not found");
			Object map = ReflectUtils.getFieldValue(threadLocalField, thread);
			if(map != null){
				Method entryMethod = ClassHelper.getClassHelper(map.getClass()).getDeclaredMethod("getEntry",ThreadLocal.class);
				Object entry = ReflectUtils.invokeMethod(map, entryMethod, threadLocal);
				if(entry != null) {
					Field valueField = ClassHelper.getClassHelper(entry.getClass()).getDeclaredField("value");
					T result = ReflectUtils.getFieldValue(valueField, entry);
					return result;
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	/**
	 * 清空线程的ThreadLocalMap
	 * @param thread
	 */
	public static void clearThreadLocalMap(Thread thread) {
		try {
			Field threadLocalField = ClassHelper.getClassHelper(Thread.class).getDeclaredField("threadLocals");
			Assert.isNotNull(threadLocalField,"field [threadLocals] not found");
			ReflectUtils.setFieldValue(threadLocalField, thread, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 參考ThreadLocal的createMap方法
	 * @param <T>
	 * @param thread
	 * @param threadLocal
	 * @param value
	 * @return
	 */
	public static <T> T createThreadLocalMap(Thread thread,ThreadLocal<T> threadLocal,T value) {
		Method method = ClassHelper.getClassHelper(ThreadLocal.class).getDeclaredMethod("createMap", Thread.class,Object.class);
		Assert.isNotNull(method,"method [createMap] not found");
		try {
			ReflectUtils.invokeMethod(threadLocal, method, thread,value);
			return value;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
