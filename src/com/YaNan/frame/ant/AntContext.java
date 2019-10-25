package com.YaNan.frame.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.consts.AntPropertyConst;
import com.YaNan.frame.ant.exception.AntInitException;
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
	volatile private static AntContext antContext;
	/**
	 * 上下文配置文件
	 */
	private AntContextConfigure contextConfigure;
	private Logger logger = LoggerFactory.getLogger(AntContext.class);
	private AntContext() {
	}
	/**
	 * 获取服务上下文
	 * @return
	 */
	public static AntContext getContext() {
		if(antContext==null) {
			synchronized (AntContext.class) {
				if(antContext==null) {
					antContext = new AntContext();
				}
			}
		}
		return antContext;
	}
	/**
	 * 初始化上下文
	 * @param file
	 */
	public void init(File file) {
		InputStream is;
		try {
			is = new FileInputStream(file);
			checkContextConfig();
			contextConfigure.setFile(file);
			this.init(is);
		} catch (FileNotFoundException e) {
			throw new AntInitException(e);
		}
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
		int port = contextConfigure.getPort();
		if(port<=0) {
			String portProp = System.getProperty(AntPropertyConst.ANT_QUEUE_PORT);
			if(portProp!=null) {
				port = Integer.valueOf(portProp);
			}
			if(port == 0) {
				port = AntDefaultConfigure.ANT_QUEUE_PORT;
			}
		}
		String host = contextConfigure.getHost();
		if(host==null) {
			host = System.getProperty(AntPropertyConst.ANT_QUEUE_HOST);
			if(host==null) {
				host = AntDefaultConfigure.ANT_QUEUE_HOST;
			}
		}
		this.init(host,port);
	}
	/**
	 * 初始化，传入端口号
	 * @param port
	 */
	public void init(String host,int port) {
		checkContextConfig();
		contextConfigure.setPort(port);
		contextConfigure.setHost(host);
		logger.debug("Plugin.ant version : 1.0.0");
		logger.debug(contextConfigure.toString());
	}
	public void start() {
		//获取
		AntRuntimeService.getAntRuntimeService().start();
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
}
