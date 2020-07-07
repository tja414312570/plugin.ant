package com.YaNan.frame.ant;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import com.YaNan.frame.ant.exception.AntInitException;
import com.YaNan.frame.ant.type.BufferType;
import com.YaNan.frame.plugin.PluginInitException;
import com.YaNan.frame.plugin.PlugsFactory.STREAM_TYPT;
import com.YaNan.frame.utils.asserts.Assert;
import com.YaNan.frame.utils.beans.xml.XMLHelper;
import com.YaNan.frame.utils.config.ConfigHelper;
import com.YaNan.frame.utils.resource.AbstractResourceEntry;
import com.YaNan.frame.utils.resource.ResourceManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class AntFactory {
	public static AntContext build(Config config) {
		Assert.isNull(config, "Ant config is null");
		Assert.isFalse(config.hasPath("Ant"), "could not found Ant config at this config "+config);
		AntContextConfigure contextConfigure = ConfigHelper.decode(config.getConfig("Ant"), AntContextConfigure.class);
		AntContext antContext = new AntContext(contextConfigure);
		return antContext;
	}
	public static AntContext build(String filePath) {
		Assert.isNull(filePath, "Ant config name is null");
		AbstractResourceEntry resourceManager =ResourceManager.getResource(filePath);
		Assert.isNull(resourceManager,"the ant config file ["+resourceManager.getPath()+"] is not exists!");
		if(filePath.endsWith(".yc")) {
			return build(resourceManager.getInputStream(),STREAM_TYPT.CONF);
		}
		if(filePath.endsWith(".xml"))
			return build(resourceManager.getInputStream(),STREAM_TYPT.XML);
		if(filePath.endsWith(".properties")) {
			return build(resourceManager.getInputStream(),STREAM_TYPT.PROPERTIES);
		}
		throw new AntInitException("the type of this file is not be support!");
	}
	public static AntContext build(InputStream inputStream,STREAM_TYPT type) {
		Assert.isNull(inputStream, "Ant config is null");
		try {
			switch(type) {
				case CONF:
					InputStreamReader reader = new InputStreamReader(inputStream);
					Config config = ConfigFactory.parseReader(reader);
					return build(config);
				case XML:
					return buildFromXml(inputStream);
				case PROPERTIES:
					try {
						Properties properties = new Properties();
						properties.load(inputStream);
						return build(properties);
					} catch (IOException e) {
						new PluginInitException(e);
					}
				default :
					throw new AntInitException("the stream type is not support for "+type);
			}
		}finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	public static AntContext build(XMLHelper xmlHelper,String resourceMark) {
		Assert.isNull(xmlHelper, "Ant config is null");
		List<AntContextConfigure> contextConfigures = xmlHelper.read();
		if(contextConfigures != null && contextConfigures.size()==0)
			throw new PluginInitException("could not pase this xml resource from "+resourceMark);
		AntContext antContext = new AntContext(contextConfigures.get(0));
		return antContext;
	}
	public static AntContext build(XMLHelper xmlHelper) {
		return build(xmlHelper, xmlHelper.toString());
	}
	public static AntContext buildFromXml(AbstractResourceEntry resource) {
		Assert.isNull(resource, "Ant config is null");
		XMLHelper xmlHelper = new XMLHelper(resource,AntContextConfigure.class);
		return build(xmlHelper, resource.getPath());
	}
	public static AntContext buildFromXml(InputStream inputStream) {
		Assert.isNull(inputStream, "Ant config is null");
		XMLHelper xmlHelper = new XMLHelper(inputStream,AntContextConfigure.class);
		return build(xmlHelper, inputStream.toString());
	}
	public static AntContext build(Properties proerties) {
		Assert.isNull(proerties, "Ant config is null");
		AntContextConfigure antConfig = new AntContextConfigure();
		ProertiesWrapper propertiesWrapper = new ProertiesWrapper(proerties);
		antConfig.setBufferMaxSize(propertiesWrapper.getInt("Ant.bufferMaxSize",2048));
		antConfig.setBufferSize(propertiesWrapper.getInt("Ant.buffer.size",1024));
		BufferType bufferType = BufferType.HEAP;
		String bufferTypeStr = propertiesWrapper.getProperty("bufferType");
		if(bufferTypeStr != null)
			bufferType = BufferType.valueOf(bufferTypeStr);
		antConfig.setBufferType(bufferType);
		antConfig.setCheckTime(propertiesWrapper.getInt("Ant.checkTime",6000));
//		antConfig.setFile(file);
		Assert.isNull(propertiesWrapper.getProperty("Ant.host"),"Ant host properties is null");
		Assert.isNull(propertiesWrapper.getProperty("Ant.name"),"Ant name properties is null");
		antConfig.setName(propertiesWrapper.getProperty("Ant.name","plugin.server"));
		antConfig.setProcess(propertiesWrapper.getInt("Ant.process", 1));
		antConfig.setPort(propertiesWrapper.getProperty("Ant.server.port", "4281"));
		antConfig.setTimeout(propertiesWrapper.getInt("Ant.timeout", 30000));
		String packages = propertiesWrapper.getProperty("Ant.package");
		if(packages == null) {
			packages = "classpath:";
		}
		antConfig.setPackages(packages.split(","));
		return new AntContext(antConfig);
	}
}
