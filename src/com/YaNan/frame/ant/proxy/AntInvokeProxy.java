package com.YaNan.frame.ant.proxy;

import java.lang.reflect.Method;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.annotations.Ant;
import com.YaNan.frame.ant.annotations.AntLock;
import com.YaNan.frame.ant.exception.AntRequestException;
import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.model.AntRequest;
import com.YaNan.frame.ant.service.AntRuntimeService;
import com.YaNan.frame.plugin.Plug;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.plugin.RegisterDescription;
import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.plugin.annotations.Support;
import com.YaNan.frame.plugin.handler.InvokeHandler;
import com.YaNan.frame.plugin.handler.MethodHandler;
import com.YaNan.frame.plugin.interfacer.PlugsListener;

/**
 * 调用远程服务的代理类
 * 
 * @author yanan
 *
 */
@Support(Ant.class)
@Register(priority = Integer.MAX_VALUE)
public class AntInvokeProxy implements PlugsListener, InvokeHandler {

	private Logger logger = LoggerFactory.getLogger(AntInvokeProxy.class);

	@Override
	public void execute(PlugsFactory plugsFactory) {
		// 从组件工厂获取所有的组件，就不必要重新扫描整个类了
		Map<Class<?>, Plug> plugs = plugsFactory.getAllPlugs();
		// 获取一个注册器实例
		RegisterDescription register = new RegisterDescription(AntInvokeProxy.class);
		// 创建一个此注册器的代理容器
		register.createProxyContainer();
		for (Plug plug : plugs.values()) {
			// 查找具有Ant注解的接口
			if (plug.getDescription().getPlugClass().getAnnotation(Ant.class) != null) {
				plug.addRegister(register);
				//需要为每个接口实现一个对应的jdk代理对象
				Object proxy = PlugsFactory.getPlugsInstanceByInsClass(plug.getDescription().getPlugClass(),
						AntInvokeProxy.class);
				// 对接口方法进行代理，代理对象为本身，目的是为了拦截方法的执行
				for (Method method : plug.getDescription().getPlugClass().getMethods()) {
					register.addMethodHandler(method, this);
				}
				// 生成代理容器中实例的key
				int hash = RegisterDescription.hash(plug.getDescription().getPlugClass());
				// 将代理对象保存到代理容器，则在调用接口的实例实际访问到自己类，其实就是为了给接口一个实例，具体有没有实现其接口并不关心
				register.getProxyContainer().put(hash, proxy);
				// 将生成的注册描述添加到接口组件

			}
		}
	}

	/**
	 * 服务中心调用的拦截
	 */
	public void before(MethodHandler methodHandler) {
		// 如果其代理类不是服务中心调用代理，则不代理此请求
		if (!methodHandler.getPlugsProxy().getProxyClass().equals(AntInvokeProxy.class))
			return;
		AntLock lock = null;
		AntClientHandler clientHandler = null;
		try {
			Class<?> clzz = methodHandler.getPlugsProxy().getInterfaceClass();
			lock = clzz.getAnnotation(AntLock.class);
			if (lock == null)
				lock = methodHandler.getMethod().getAnnotation(AntLock.class);
			Ant rpc = clzz.getAnnotation(Ant.class);
			AntRequest request = new AntRequest();
			request.setService(rpc.value());
			request.setInvokeClass(clzz);
			request.setInvokeParmeters(methodHandler.getParameters());
			request.setInvokeMethod(methodHandler.getMethod());
			clientHandler = AntClientHandler.getHandler();
			//如果没有预先设置
			if(clientHandler == null) 
				clientHandler = AntRuntimeService.getAntRuntimeService().getClientHandler(rpc.value());
			if(clientHandler == null)
				throw new AntRequestException("could not found client handler for '"+rpc.value()+"'");
			Object object = AntRuntimeService.getAntRuntimeService().request(clientHandler,request, lock != null);
			methodHandler.interrupt(object);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			if (lock != null && lock.auto()
					&& clientHandler != null)
				clientHandler.releaseWriteLock();
		}
	}

	@Override
	public void after(MethodHandler methodHandler) {
	}

	@Override
	public void error(MethodHandler methodHandler, Throwable e) {

	}

}
