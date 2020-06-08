package com.YaNan.frame.ant.implement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.annotations.Ant;
import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.interfaces.customer.AntClientService;
import com.YaNan.frame.ant.model.AntCustomer;
import com.YaNan.frame.ant.model.RegisterResult;
import com.YaNan.frame.plugin.ProxyModel;
import com.YaNan.frame.plugin.annotations.Register;

/**
 * 默认Ant服务连接
 * @author yanan
 *
 */
@Ant
@Register(model=ProxyModel.CGLIB)
public class DefaultAntClientServiceImpl implements  AntClientService{
	
	private Logger logger = LoggerFactory.getLogger(DefaultAntClientServiceImpl.class);
	@Override
	public RegisterResult registClient(AntCustomer antCustomer) {
		logger.debug("register ant customer "+antCustomer);
		AntClientHandler.getHandler().getRuntimeService().
		getAntClientRegisterService().registerSuccess(AntClientHandler.getHandler());
		RegisterResult result = new RegisterResult();
		result.setSid(AntClientHandler.getHandler().getId());
		result.setStatus(200);
		logger.debug("register result "+result);
		return result;
	}
}
