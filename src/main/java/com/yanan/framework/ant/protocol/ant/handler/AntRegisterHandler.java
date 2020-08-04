package com.yanan.framework.ant.protocol.ant.handler;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.framework.ant.exception.AntRegisterRuntimeException;
import com.yanan.framework.ant.model.AntCustomer;
import com.yanan.framework.ant.model.AntProviderSummary;
import com.yanan.framework.ant.model.AntRequest;
import com.yanan.framework.ant.model.RegisterResult;
import com.yanan.framework.ant.protocol.ant.AntClientInstance;
import com.yanan.framework.ant.protocol.ant.interfacer.AntClientCommandService;
import com.yanan.framework.ant.protocol.ant.interfacer.AntRegisterSatus;
import com.yanan.framework.ant.utils.ObjectLock;
import com.yanan.utils.reflect.cache.ClassHelper;

/**
 * Ant注册处理实现
 * @author yanan
 *
 */
public class AntRegisterHandler extends AntAbstractRegisterHandler{
	private Logger logger = LoggerFactory.getLogger(AntRegisterHandler.class);
	public AntRegisterHandler(AntClientInstance handler) {
		super(handler);
	}
	@Override
	public void regist() {
		try {
			//组装注册信息
			AntRequest request = new AntRequest();
			request.setInvokeClass(AntClientCommandService.class);
			AntCustomer customer = new AntCustomer();
			//设置自己的名字//如果名字为空。。。我也不知道填啥玩意
			customer.setName(clientInstance.getRuntimeService().getContextConfigure().getName());
			//设置附加数据
			customer.setAttach("附加数据");
			request.setInvokeParmeters(customer);
			logger.debug("register info :"+customer);
			Method method = ClassHelper.getClassHelper(AntClientCommandService.class).getMethod("registClient", AntCustomer.class);
			request.setInvokeMethod(method);
			request.setTimeout(this.clientInstance.getRuntimeService().getContextConfigure().getTimeout());
			RegisterResult result = (RegisterResult) clientInstance.getRuntimeService().request(clientInstance.getServiceInstance(),request, true);
			logger.debug("regist result:"+result);
			//从注册列表删除此注册
			clientInstance.getClientService().getAntRegisterServcie().removeRegister(this);
			AntProviderSummary sum = this.clientInstance.getServiceInstance().getAttribute(AntProviderSummary.class);
			//判断是否注册成功
			if(result.getStatus() == AntRegisterSatus.SUCCESS) {
				logger.debug("Register to service success!"+this.clientInstance);
				if(sum!=null) {
					clientInstance.getRuntimeService().addClientHandler(sum.getName(),this.clientInstance.getServiceInstance());
				}
			}else {
				logger.debug("Regist to service failed!");
				if(sum!=null) {
					this.clientInstance.getServiceInstance().close("failed to regist to service!["+sum.getName()+"]");
				}else {
					this.clientInstance.getServiceInstance().close("failed to regist to service!["+clientInstance.getRemoteAddress()+"]");
				}
				
			}
			this.clientInstance.getServiceInstance().releaseWriteLock();
		}catch(Throwable t) {
			AntProviderSummary sum = this.clientInstance.getServiceInstance().getAttribute(AntProviderSummary.class);
			ObjectLock.getLock(sum.getName()).release();
			throw new AntRegisterRuntimeException(t.getMessage(),t);
		}
	
	}

}