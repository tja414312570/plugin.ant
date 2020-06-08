package com.YaNan.frame.ant.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target(ElementType.TYPE )
@Retention(RetentionPolicy.RUNTIME)
public @interface AntQueen {
	/**
	 * 响应超时 默认30s 无使用
	 * @return
	 */
	int timeout() default 30*1000;
	}
