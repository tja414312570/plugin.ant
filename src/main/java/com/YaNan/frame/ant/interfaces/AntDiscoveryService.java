package com.YaNan.frame.ant.interfaces;

import java.util.List;

import com.YaNan.frame.ant.annotations.Ant;
import com.YaNan.frame.ant.annotations.AntLock;
//import com.YaNan.frame.ant.annotations.AntQueen;
import com.YaNan.frame.ant.model.AntProvider;
import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.plugin.annotations.Service;

/**
 * 服务中心功能
 * @author yanan
 *
 */
@Ant//表明此接口可以通过Ant代理
@AntLock(auto = false) //注解表明加锁，auto = false表示不自动释放锁
@Service //表明需要被代理
public interface AntDiscoveryService {
	/**
	 * 下载服务
	 * @param name 服务名
	 * @return
	 */
	List<AntProviderSummary>  downloadProviderList(String name) throws Exception;
	/**
	 * 提供端注册服务
	 */
	void registerService(AntProvider antProvider) throws Exception;
	/**
	 * 判断服务提供是否可用
	 * @return
	 */
	void avaiable() throws Exception;
	
}
