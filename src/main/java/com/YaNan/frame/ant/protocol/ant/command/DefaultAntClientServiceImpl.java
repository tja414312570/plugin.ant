package com.YaNan.frame.ant.protocol.ant.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.annotations.Ant;
import com.YaNan.frame.ant.handler.AntServiceInstance;
import com.YaNan.frame.ant.model.AntCustomer;
import com.YaNan.frame.ant.model.RegisterResult;
import com.YaNan.frame.ant.protocol.ant.AntClientInstance;
import com.YaNan.frame.ant.protocol.ant.interfacer.AntClientCommandService;
import com.YaNan.frame.plugin.ProxyModel;
import com.YaNan.frame.plugin.annotations.Register;

/**
 * 默认Ant服务连接
 * @author yanan
 *
 */
@Ant
@Register(model=ProxyModel.CGLIB)
public class DefaultAntClientServiceImpl implements  AntClientCommandService{
	private Logger logger = LoggerFactory.getLogger(DefaultAntClientServiceImpl.class);
	@Override
	public RegisterResult registClient(AntCustomer antCustomer) {
		logger.debug("register ant customer "+antCustomer);
		AntClientInstance clientInstance = AntServiceInstance.getServiceInstance().getAttribute(AntClientInstance.class);
		clientInstance.getClientService().getAntClientRegisterService().registerSuccess(clientInstance);
		RegisterResult result = new RegisterResult();
		result.setSid(AntServiceInstance.getServiceInstance().getId());
		result.setStatus(200);
		logger.debug("register result "+result);
		return result;
	}
}
