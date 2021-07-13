package com.yanan.framework.a.proxy;

import org.slf4j.Logger;

import com.yanan.framework.a.dispatcher.ChannelDispatcher;
import com.yanan.framework.ant.annotations.AntLock;
import com.yanan.framework.ant.handler.AntServiceInstance;
import com.yanan.framework.ant.model.AntRequest;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.framework.plugin.annotations.Support;
import com.yanan.framework.plugin.handler.InvokeHandler;
import com.yanan.framework.plugin.handler.MethodHandler;

/**
 * 调用远程服务的代理类
 * 
 * @author yanan
 *
 */
@Support(Ant.class)
@Register(priority = Integer.MAX_VALUE)
public class InvokeProxyer implements InvokeHandler,InvokeProxy{
	@Service
	private Logger logger;
	@Service
	private ChannelDispatcher<String> channelDispatcher;
	/**
	 * 服务中心调用的拦截
	 */
	public void before(MethodHandler methodHandler) {
		// 如果其代理类不是服务中心调用代理，则不代理此请求
		if (!methodHandler.getPlugsProxy().getProxyClass().equals(InvokeProxyer.class))
			return;
		AntLock lock = null;
		AntServiceInstance clientHandler = null;
		try {
			Class<?> clzz = methodHandler.getPlugsProxy().getInterfaceClass();
			lock = clzz.getAnnotation(AntLock.class);
			if (lock == null)
				lock = methodHandler.getMethod().getAnnotation(AntLock.class);
			Ant rpc = clzz.getAnnotation(Ant.class);
			Invokers invokers = new Invokers();
			invokers.setInvokeClass(clzz);
			invokers.setInvokeParmeters(methodHandler.getParameters());
			invokers.setInvokeMethod(methodHandler.getMethod());
			Object result = channelDispatcher.request(rpc.value(),invokers);
			System.err.println("远程返回结果:"+result);
			methodHandler.interrupt(result);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			AntServiceInstance.removeHandler();
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

	@SuppressWarnings("unchecked")
	@Override
	public void bind(ChannelDispatcher<?> channelDispatcher) {
		this.channelDispatcher = (ChannelDispatcher<String>) channelDispatcher;
	}

}