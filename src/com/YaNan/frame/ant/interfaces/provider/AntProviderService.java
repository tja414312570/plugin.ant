package com.YaNan.frame.ant.interfaces.provider;

import java.util.List;

import com.YaNan.frame.ant.annotations.Ant;
import com.YaNan.frame.ant.annotations.AntLock;
//import com.YaNan.frame.ant.annotations.AntQueen;
import com.YaNan.frame.ant.model.AntCustomer;
import com.YaNan.frame.ant.model.AntProvider;
import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.ant.model.RegisterResult;
import com.YaNan.frame.plugin.annotations.Service;

/**
 * 服务中心功能
 * @author yanan
 *
 */
@Ant//表明此接口可以通过Ant代理
@AntLock(auto = false) //注解表明加锁，auto = false表示不自动释放锁
@Service //表明需要被代理
public interface AntProviderService {
	/**
	 * 消费端注册
	 * @param antContextConfigure
	 * @return
	 */
	RegisterResult registCustomer(AntCustomer antCustomer);
	/**
	 * 下载服务
	 * @param name 服务名
	 * @return
	 */
	List<AntProviderSummary>  downloadProviderList(String name);
	/**
	 * 提供端注册服务
	 */
	RegisterResult registProvider(AntProvider antProvider);
	
}
