package com.yanan.framework.a.proxy;

import java.lang.reflect.Method;

import org.slf4j.Logger;

import com.yanan.framework.a.dispatcher.ChannelDispatcher;
import com.yanan.framework.plugin.Plugin;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.ProxyModel;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.framework.plugin.builder.PluginDefinitionBuilderFactory;
import com.yanan.framework.plugin.builder.PluginInstanceFactory;
import com.yanan.framework.plugin.definition.RegisterDefinition;
import com.yanan.framework.plugin.handler.InvokeHandler;
import com.yanan.utils.resource.scanner.PackageScanner;
import com.yanan.utils.resource.scanner.PackageScanner.ClassInter;

@Register(id = "proxyInvokerMapperBuilder")
public class ProxyInvokerMapperBuilder implements ProxyInvokerMapper{
	@Service
	private Logger logger;
	private InvokeProxy antInvokeProxy;
	private String[] scanPath = { "classpath:**" };
	private ChannelDispatcher channelDispatcher;

	public void initProxy() {
		logger.debug("proxy invoke mapper start");
		antInvokeProxy = PlugsFactory.getPluginsInstance(InvokeProxy.class);
		// 从组件工厂获取所有的组件，就不必要重新扫描整个类了
		RegisterDefinition register = PluginDefinitionBuilderFactory.builderRegisterDefinition(InvokeProxyer.class);
		logger.debug("proxy register " + register);
		// 创建一个此注册器的代理容器
		register.createProxyContainer();
//		ProxyModel.JDK
		register.setProxyModel(ProxyModel.JDK);
		// 扫描
		PackageScanner packageScanner = new PackageScanner();
		packageScanner.addScanPath(scanPath);
		packageScanner.doScanner(new ClassInter() {
			// 获取一个注册器实例
			@Override
			public void find(Class<?> cls) {
				Ant ant = cls.getAnnotation(Ant.class);
				// 查找具有Ant注解的接口
				if (ant != null) {
					Plugin plug = PlugsFactory.getPlugin(cls);
					if (plug == null) {
						plug = PluginDefinitionBuilderFactory.builderPluginDefinition(cls);
					}
					plug.addRegister(register);
					PlugsFactory.getInstance().addPlugininDefinition(plug);
					// 需要为每个接口实现一个对应的jdk代理对象
					InvokeProxy proxy = (InvokeProxy) PlugsFactory.getPluginsInstanceByInsClass(cls, InvokeProxyer.class);
					System.err.println("proxy:"+proxy);
					proxy.bind(channelDispatcher);
					// 对接口方法进行代理，代理对象为本身，目的是为了拦截方法的执行
					for (Method method : plug.getDefinition().getPlugClass().getMethods()) {
						register.addMethodHandler(method, (InvokeHandler) antInvokeProxy);
					}
					// 生成代理容器中实例的key
					int hash = PluginInstanceFactory.hash(plug.getDefinition().getPlugClass());
					// 将代理对象保存到代理容器，则在调用接口的实例实际访问到自己类，其实就是为了给接口一个实例，具体有没有实现其接口并不关心
					register.getProxyContainer().put(hash, proxy);
				}
			}
		});
	}

	public String[] getScanPath() {
		return scanPath;
	}

	public void setScanPath(String[] scanPath) {
		this.scanPath = scanPath;
	}

	@Override
	public void bind(ChannelDispatcher channelDispatcher) {
		this.channelDispatcher = channelDispatcher;
		initProxy();
	}
}