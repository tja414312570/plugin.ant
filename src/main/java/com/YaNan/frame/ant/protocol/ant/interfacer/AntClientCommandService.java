package com.YaNan.frame.ant.protocol.ant.interfacer;

import com.YaNan.frame.ant.model.AntCustomer;
import com.YaNan.frame.ant.model.RegisterResult;

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
