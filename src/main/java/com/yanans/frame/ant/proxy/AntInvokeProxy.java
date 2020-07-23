package com.yanan.frame.ant.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.ant.annotations.Ant;
import com.yanan.frame.ant.annotations.AntLock;
import com.yanan.frame.ant.exception.AntRequestException;
import com.yanan.frame.ant.handler.AntServiceInstance;
import com.yanan.frame.ant.model.AntRequest;
import com.yanan.frame.ant.service.AntRuntimeService;
import com.yanan.frame.plugin.annotations.Register;
import com.yanan.frame.plugin.annotations.Support;
import com.yanan.frame.plugin.handler.InvokeHandler;
import com.yanan.frame.plugin.handler.MethodHandler;

/**
 * 调用远程服务的代理类
 * 
 * @author yanan
 *
 */
@Support(Ant.class)
@Register(priority = Integer.MAX_VALUE)
public class AntInvokeProxy implements InvokeHandler {

	private Logger logger = LoggerFactory.getLogger(AntInvokeProxy.class);
	private AntRuntimeService runtimeService;
	public AntRuntimeService getRuntimeService() {
		return runtimeService;
	}
	public AntInvokeProxy(AntRuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}
	/**
	 * 服务中心调用的拦截
	 */
	public void before(MethodHandler methodHandler) {
		// 如果其代理类不是服务中心调用代理，则不代理此请求
		if (!methodHandler.getPlugsProxy().getProxyClass().equals(AntInvokeProxy.class))
			return;
		AntLock lock = null;
		AntServiceInstance clientHandler = null;
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
			request.setType(rpc.type());
			request.setTimeout(rpc.timeout() < -1 ? runtimeService.getContextConfigure().getTimeout():rpc.timeout());
			clientHandler = runtimeService.getClientHandler(rpc.value());
			if(clientHandler == null)
				throw new AntRequestException("could not found client handler for '"+rpc.value()+"'");
			Object object = runtimeService.request(clientHandler,request, lock != null);
			methodHandler.interrupt(object);
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

}