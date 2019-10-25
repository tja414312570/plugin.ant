package com.YaNan.frame.ant.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 用于获取当前的连接端
 * @author yanan
 */
@Target(ElementType.FIELD )
@Retention(RetentionPolicy.RUNTIME)
public @interface AntClient {
}
