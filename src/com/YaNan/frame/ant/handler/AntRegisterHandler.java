package com.YaNan.frame.ant.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.AntContext;
import com.YaNan.frame.ant.abstracts.AntAbstractRegisterHandler;
import com.YaNan.frame.ant.interfaces.provider.AntProviderService;
import com.YaNan.frame.ant.interfaces.provider.AntRegisterSatus;
import com.YaNan.frame.ant.model.AntCustomer;
import com.YaNan.frame.ant.model.AntProvider;
import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.ant.model.RegisterResult;
import com.YaNan.frame.ant.proxy.AntInvokeProxy;
import com.YaNan.frame.ant.service.AntRegisterService;
import com.YaNan.frame.ant.service.AntRuntimeService;
import com.YaNan.frame.ant.type.ClientType;
import com.YaNan.frame.plugin.PlugsFactory;

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
		//拿到服务器注册的接口的带来
		AntProviderService service = PlugsFactory.getPlugsInstanceByInsClass(AntProviderService.class,AntInvokeProxy.class);
		RegisterResult result = null;
		logger.debug("register type :"+(AntContext.getContext().getContextConfigure().getServerPort()>0?"provider":"customer"));
		//拿到当前线程所属连接
		AntClientHandler.setHandler(this.clientHandler);
		//判断注册类型   注册到服务端
		if(this.clientHandler.getClientType() == ClientType.Provider) {
			//当自身为服务端时   此时当然是注册到服务中心
			AntProvider provider = new AntProvider();
			//设置自己对位服务名字
			provider.setName(AntContext.getContext().getContextConfigure().getName());
			//设置自己的服务端口号
			provider.setPort(AntContext.getContext().getContextConfigure().getServerPort());
			provider.setHost(AntContext.getContext().getContextConfigure().getHost());
			//注册到注册中心
			result = service.registProvider(provider);
			logger.debug("register info :"+provider);
		}else{
			//当自己问消费端时，可能注册到服务端，也可能注册到服务中心
			//此时只要携带数据就OK
			AntCustomer customer = new AntCustomer();
			//设置自己的名字//如果名字为空。。。我也不知道填啥玩意
			customer.setName(AntContext.getContext().getContextConfigure().getName());
			//设置附加数据
			customer.setAttach("附加数据");
			//调用注册方法
			result = service.registCustomer(customer);
			logger.debug("register info :"+customer);
		}
		logger.debug("Queen regist result:"+result);
		//从注册列表删除此注册
		AntRegisterService.getInstance().removeRegister(this);
		//判断是否注册成功
		if(result.getStatus() == AntRegisterSatus.SUCCESS) {
			logger.debug("Register to queen success!"+this.clientHandler);
			AntProviderSummary sum = this.clientHandler.getAttribute(AntProviderSummary.class);
			if(sum!=null) {
				AntRuntimeService.getAntRuntimeService().addClientHandler(sum.getName(),this.clientHandler);
			}
			//通知运行时上下文环境连接到服务中心成功
			AntRuntimeService.getAntRuntimeService().started();
		}else {
			AntRegisterService.getInstance().setAvailable(false);
			logger.debug("Regist to queen failed!");
			this.clientHandler.close("failed to regist to Queen");
		}
		this.clientHandler.releaseWriteLock();
		AntRegisterService.getInstance().releaseLock();
	}

}
