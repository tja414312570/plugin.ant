package com.YaNan.frame.ant.interfaces.customer;

import com.YaNan.frame.ant.model.AntCustomer;
import com.YaNan.frame.ant.model.RegisterResult;

/**
 * Ant连接功能
 * @author yanan
 *
 */
public interface AntClientService {
	/**
	 * 注册服务
	 * @param antContextConfigure
	 * @return
	 */
	RegisterResult registClient(AntCustomer antCustomer);
}
