package com.YaNan.frame.ant.proxy;

import java.lang.reflect.Method;
import com.YaNan.frame.ant.annotations.Ant;
import com.YaNan.frame.ant.exception.AntInitException;
import com.YaNan.frame.ant.service.AntRuntimeService;
import com.YaNan.frame.plugin.Plug;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.plugin.ProxyModel;
import com.YaNan.frame.plugin.RegisterDescription;
import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.utils.resource.PackageScanner;
import com.YaNan.frame.utils.resource.PackageScanner.ClassInter;

@Register(priority = Integer.MAX_VALUE,init = "execute",model=ProxyModel.CGLIB)
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
		antInvokeProxy = PlugsFactory.getPlugsInstance(AntInvokeProxy.class, antRuntimeService);
		
		// 从组件工厂获取所有的组件，就不必要重新扫描整个类了
		RegisterDescription register = new RegisterDescription(AntInvokeProxy.class);
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
						Plug plug = PlugsFactory.getPlug(cls);
						if(plug == null) {
							PlugsFactory.getInstance().addPlugsAuto(cls);
							plug = PlugsFactory.getPlug(cls);
						}
						plug.addRegister(register);
						//需要为每个接口实现一个对应的jdk代理对象
						Object proxy = PlugsFactory.getPlugsInstanceByInsClass(plug.getDescription().getPlugClass(),
								AntInvokeProxy.class,new Class<?>[]{AntRuntimeService.class},antRuntimeService);
						// 对接口方法进行代理，代理对象为本身，目的是为了拦截方法的执行
						for (Method method : plug.getDescription().getPlugClass().getMethods()) {
							register.addMethodHandler(method, antInvokeProxy);
						}
						// 生成代理容器中实例的key
						int hash = RegisterDescription.hash(plug.getDescription().getPlugClass());
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
