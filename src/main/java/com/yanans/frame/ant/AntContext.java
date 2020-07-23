package com.yanan.frame.ant;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.ant.service.AntRuntimeService;
import com.yanan.utils.asserts.Assert;
import com.yanan.utils.string.StringUtil;

/**
 * 服务中心上下文
 * @author yanan
 *
 */
public class AntContext {
	/**
	 * 上下文配置文件
	 */
	private AntContextConfigure contextConfigure;
	private Logger logger = LoggerFactory.getLogger(AntContext.class);
	private AntRuntimeService antRuntimeService;
	public AntContext(AntContextConfigure contextConfigure) {
		this.contextConfigure = contextConfigure;
	}
	public void setContextConfigure(AntContextConfigure contextConfigure) {
		this.contextConfigure = contextConfigure;
	}
	/**
	 * 初始化，传入端口号
	 * @param port
	 */
	public void start() {
		Assert.isNull(this.contextConfigure,"ant configure is null!");
		if(StringUtil.isEmpty(this.contextConfigure.getHost())) {
			logger.debug("ant host is empty,try to get host!");
			try {
				this.contextConfigure.setHost(InetAddress.getLocalHost().getHostAddress());
				logger.debug("get service host : " + this.contextConfigure.getHost());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		logger.debug("start ant with "+this.contextConfigure);
		//从nacos注册服务
		this.setAntRuntimeService(new AntRuntimeService(this));
		this.antRuntimeService.start();
	}
	public AntContextConfigure getContextConfigure() {
		return contextConfigure;
	}
	public AntRuntimeService getAntRuntimeService() {
		return antRuntimeService;
	}
	public void setAntRuntimeService(AntRuntimeService antRuntimeService) {
		this.antRuntimeService = antRuntimeService;
	}
}