package com.YaNan.frame.ant.handler;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.AntContextConfigure;
import com.YaNan.frame.ant.abstracts.AntAbstractRegisterHandler;
import com.YaNan.frame.ant.exception.AntRegisterRuntimeException;
import com.YaNan.frame.ant.interfaces.customer.AntClientService;
import com.YaNan.frame.ant.interfaces.provider.AntRegisterSatus;
import com.YaNan.frame.ant.model.AntCustomer;
import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.ant.model.AntRequest;
import com.YaNan.frame.ant.model.RegisterResult;
import com.YaNan.frame.utils.reflect.cache.ClassHelper;

/**
 * Ant注册处理实现
 * @author yanan
 *
 */
public class AntRegisterHandler extends AntAbstractRegisterHandler{
	private Logger logger = LoggerFactory.getLogger(AntRegisterHandler.class);
	public AntRegisterHandler(AntClientHandler handler) {
		super(handler);
	}
	@Override
	public void regist() {
		try {
			//组装注册信息
			AntContextConfigure config = this.clientHandler.getRuntimeService().getContextConfigure();
			AntRequest request = new AntRequest();
			request.setService(config.getName());
			request.setInvokeClass(AntClientService.class);
			AntCustomer customer = new AntCustomer();
			//设置自己的名字//如果名字为空。。。我也不知道填啥玩意
			customer.setName(clientHandler.getRuntimeService().getContextConfigure().getName());
			//设置附加数据
			customer.setAttach("附加数据");
			request.setInvokeParmeters(customer);
			Method method = ClassHelper.getClassHelper(AntClientService.class).getMethod("registClient", AntCustomer.class);
			request.setInvokeMethod(method);
			RegisterResult result = (RegisterResult) clientHandler.getRuntimeService().request(clientHandler,request, true);
			logger.debug("register info :"+customer);
			logger.debug("Queen regist result:"+result);
			//从注册列表删除此注册
			clientHandler.getRuntimeService().getAntRegisterServcie().removeRegister(this);
			//判断是否注册成功
			if(result.getStatus() == AntRegisterSatus.SUCCESS) {
				logger.debug("Register to service success!"+this.clientHandler);
				AntProviderSummary sum = this.clientHandler.getAttribute(AntProviderSummary.class);
				if(sum!=null) {
					clientHandler.getRuntimeService().addClientHandler(sum.getName(),this.clientHandler);
				}
			}else {
				clientHandler.getRuntimeService().getAntRegisterServcie().setAvailable(false);
				logger.debug("Regist to service failed!");
				this.clientHandler.close("failed to regist to Queen");
			}
			this.clientHandler.releaseWriteLock();
			clientHandler.getRuntimeService().getAntRegisterServcie().releaseLock();
		}catch(Throwable t) {
			throw new AntRegisterRuntimeException(t.getMessage(),t);
		}
	
	}

}
