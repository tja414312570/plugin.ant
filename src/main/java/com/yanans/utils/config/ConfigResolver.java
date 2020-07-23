package com.yanan.utils.config;

import java.lang.reflect.Field;

import com.typesafe.config.Config;

/**
 * 配置解析
 * @author yanan
 *
 */
public interface ConfigResolver {
	/**
	 * 自定义配置解析
	 * @param field
	 * @param config
	 * @param instance
	 * @return
	 */
	Object decode(Field field,Config config,Object instance);
}