package com.YaNan.frame.ant.interfaces;

import com.YaNan.frame.ant.handler.AntServiceInstance;
import com.YaNan.frame.ant.model.AntMessagePrototype;

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
