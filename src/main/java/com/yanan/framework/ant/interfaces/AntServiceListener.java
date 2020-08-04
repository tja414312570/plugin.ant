package com.yanan.framework.ant.interfaces;

import com.yanan.framework.ant.handler.AntServiceInstance;
import com.yanan.framework.ant.model.AntMessagePrototype;

/**
 * Ant服务监听
 * @author yanan
 *
 */
public interface AntServiceListener {
	/**
	 * 写入消息
	 * @param messagePrototype
	 * @param serviceInstance
	 */
	void onWrite(AntMessagePrototype messagePrototype,AntServiceInstance serviceInstance);
}