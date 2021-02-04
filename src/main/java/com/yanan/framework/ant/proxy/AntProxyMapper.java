package com.yanan.framework.ant.proxy;

import java.lang.reflect.Method;

import com.yanan.framework.ant.annotations.Ant;
import com.yanan.framework.ant.exception.AntInitException;
import com.yanan.framework.ant.service.AntRuntimeService;
import com.yanan.framework.plugin.Plugin;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.ProxyModel;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.builder.PluginDefinitionBuilderFactory;
import com.yanan.framework.plugin.builder.PluginInstanceFactory;
import com.yanan.framework.plugin.definition.RegisterDefinition;
import com.yanan.utils.resource.scanner.PackageScanner;
import com.yanan.utils.resource.scanner.PackageScanner.ClassInter;

@Register(priority = Integer.MAX_VALUE,afterInstance = "execute",model=ProxyModel.CGLIB)
public class AntProxyMapper {
	/**
	 * 运行时
	 */
	private AntRuntimeService antRuntimeService;
	
	public AntRuntimeService getRuntimeService() {
		return antRuntimeService;
	}
	public AntProxyMapper(AntRuntimeService antRuntimeService,String[] scanPath) {
		super();
		this.scanPath = scanPath;
		this.antRuntimeService = antRuntimeService;
	}
	public void setAntRuntimeService(AntRuntimeService antRuntimeService) {
		this.antRuntimeService = antRuntimeService;
	}
	private AntInvokeProxy antInvokeProxy;
	/**
	 * 扫描路径
	 */
	private String[] scanPath;
	public void execute() {
		if(antRuntimeService == null)
			throw new AntInitException("not Ant Runtime Context for the ant mapper");
		antInvokeProxy = PlugsFactory.getPluginsInstance(AntInvokeProxy.class, antRuntimeService);
		System.out.println(antRuntimeService.getContextConfigure().getPackages());
		// 从组件工厂获取所有的组件，就不必要重新扫描整个类了
		RegisterDefinition register = PluginDefinitionBuilderFactory.builderRegisterDefinition(AntInvokeProxy.class);
		// 创建一个此注册器的代理容器
		register.createProxyContainer();
		//扫描
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
						if(plug == null) {
							plug = PluginDefinitionBuilderFactory.builderPluginDefinition(cls);
						}
						plug.addRegister(register);
						PlugsFactory.getInstance().addPlugininDefinition(plug);
						//需要为每个接口实现一个对应的jdk代理对象
						Object proxy = PlugsFactory.getPluginsInstanceByInsClass(cls,AntInvokeProxy.class,new Class<?>[]{AntRuntimeService.class},antRuntimeService);
						// 对接口方法进行代理，代理对象为本身，目的是为了拦截方法的执行
						for (Method method : plug.getDefinition().getPlugClass().getMethods()) {
							register.addMethodHandler(method, antInvokeProxy);
						}
						// 生成代理容器中实例的key
						int hash = PluginInstanceFactory.hash(plug.getDefinition().getPlugClass());
						// 将代理对象保存到代理容器，则在调用接口的实例实际访问到自己类，其实就是为了给接口一个实例，具体有没有实现其接口并不关心
						register.getProxyContainer().put(hash, proxy);
						// 将生成的注册描述添加到接口组件
//						String name = ant.value();
//						if(name != null) {
//							AntClientHandler handler = antRuntimeService.getClientHandler(name);
//							if(handler == null) {
//								
//							}
//						}
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
}