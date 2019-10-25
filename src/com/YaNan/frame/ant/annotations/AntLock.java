package com.YaNan.frame.ant.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ant连接锁定
 * @author yanan
 */
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AntLock {
	/**
	 * 自动释放
	 * 如果是代理调用方式，请勿修改，否则造成死锁
	 * @return
	 */
	boolean auto() default true;
}
