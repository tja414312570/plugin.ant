package com.YaNan.frame.ant.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.YaNan.frame.utils.asserts.Assert;

/**
 * 一个条件锁
 * @author yanan
 *
 */
public class ObjectLock {
	private static Map<Object,ObjectLock> lockMap = new ConcurrentHashMap<>();
	private ObjectLock(int timeout,Object lockObject) {
		this.timeout = timeout;
		this.lockObject = lockObject;
	};
	private int timeout;
	private Object lockObject;
	private volatile AtomicBoolean atomicLock = new AtomicBoolean(false);
	public static ObjectLock getLock(Object lockObject,int timeout) {
		Assert.isNull(lockObject);
		if(lockMap.containsKey(lockObject))
			return lockMap.get(lockObject);
		synchronized (lockObject) {
			if(lockMap.containsKey(lockObject))
				return lockMap.get(lockObject);
			ObjectLock lock = new ObjectLock(timeout,lockObject);
			lockMap.put(lockObject, lock);
			return lock;
		}
	}
	public static ObjectLock getLock(Object lockObject) {
		Assert.isNull(lockObject);
		if(lockMap.containsKey(lockObject))
			return lockMap.get(lockObject);
		throw new LockNotFoundException(lockObject);
	}
	public void tryLock() {
		System.out.println("=======================获取锁"+this.lockObject+"  "+atomicLock.get());
		System.out.println(this);
		long now = System.currentTimeMillis();
		while(!atomicLock.compareAndSet(false, true)) {
			if((System.currentTimeMillis() - now ) / 1000 > timeout)
				throw new LockTimeoutException(lockObject,timeout,true);
		}
		System.out.println("=======================++++++++");
	}
	public void release() {
		System.out.println("=======================释放锁锁"+this.lockObject+"  "+atomicLock.get());
		System.out.println(this);
		long now = System.currentTimeMillis();
		while(!atomicLock.compareAndSet(true, false)) {
			if((System.currentTimeMillis() - now ) / 1000 > timeout)
				throw new LockTimeoutException(lockObject,timeout,false);
		}
	}
}
