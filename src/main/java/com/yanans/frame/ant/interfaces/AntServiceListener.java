package com.yanan.frame.ant.interfaces;

import com.yanan.frame.ant.handler.AntServiceInstance;
import com.yanan.frame.ant.model.AntMessagePrototype;

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