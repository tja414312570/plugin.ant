package com.YaNan.frame.ant;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.service.AntRuntimeService;
import com.YaNan.frame.plugin.ConfigContext;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.plugin.PlugsFactory.STREAM_TYPT;
import com.YaNan.frame.utils.config.ConfigHelper;
import com.typesafe.config.Config;

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
	/**
	 * 初始化上下文
	 * @param is
	 */
	public void init(InputStream is) {
		PlugsFactory.getInstance().addPlugs(is, STREAM_TYPT.CONF, null);
		checkContextConfig();
		Config config = ConfigContext.getInstance().getGlobalConfig().getConfig("Ant");
		if(config!=null) {
			contextConfigure = ConfigHelper.decode(config, AntContextConfigure.class);
		}
		this.init();
	}
	/**
	 * 初始化上下文
	 */
	public void init() {
		checkContextConfig();
	}
	/**
	 * 初始化，传入端口号
	 * @param port
	 */
	public void start() {
		logger.debug("start ant with "+this.contextConfigure);
		//从nacos注册服务
		this.setAntRuntimeService(new AntRuntimeService(this));
		this.antRuntimeService.start();
		//获取
//		AntRuntimeService.getAntRuntimeService().start();
	}
	/**
	 * 检查配置
	 */
	private void checkContextConfig() {
		if(contextConfigure == null)
			synchronized (this) {
				if(contextConfigure == null) {
					contextConfigure = new AntContextConfigure();
				}
			}
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
