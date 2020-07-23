package com.yanan.frame.ant.interfaces;

import java.util.List;

import com.yanan.frame.ant.model.AntProvider;
import com.yanan.frame.ant.model.AntProviderSummary;
import com.yanan.frame.ant.service.AntRuntimeService;
import com.yanan.frame.plugin.annotations.Service;

/**
 * 服务中心接口
 * @author yanan
 *
 */
@Service //表明需要被代理
public interface AntDiscoveryService {
	/**
	 * 下载服务
	 * @param name 服务名
	 * @return
	 */
	AntProviderSummary getService(String name) throws Exception;
	/**
	 * 下载服务
	 * @param name 服务名
	 * @return
	 */
	List<AntProviderSummary> downloadProviderList(String name) throws Exception;
	/**
	 * 提供端注册服务
	 */
	void registerService(AntProvider antProvider) throws Exception;
	/**
	 * 判断服务提供是否可用
	 */
	void avaiable() throws Exception;
	/**
	 * 注册AntRuntimeService
	 */
	void setAntRuntimeService(AntRuntimeService runtimeService);
	/**
	 * 注销服务
	 * @param providerSummary
	 */
	void deregisterService(AntProviderSummary providerSummary) throws Exception;
}