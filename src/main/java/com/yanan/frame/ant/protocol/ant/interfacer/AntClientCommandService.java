package com.yanan.frame.ant.protocol.ant.interfacer;

import com.yanan.frame.ant.model.AntCustomer;
import com.yanan.frame.ant.model.RegisterResult;

/**
 * Ant连接功能
 * @author yanan
 *
 */
public interface AntClientCommandService {
	/**
	 * 注册服务
	 * @param antContextConfigure
	 * @return
	 */
	RegisterResult registClient(AntCustomer antCustomer);
}