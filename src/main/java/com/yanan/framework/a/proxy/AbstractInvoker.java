package com.yanan.framework.a.proxy;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.dispatcher.ChannelDispatcher;
import com.yanan.framework.a.dispatcher.DispatcherContext;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;

@Register
public class AbstractInvoker implements Invoker<DispatcherContext<Invokers>>{
	@Service
	private ProxyInvokerMapper proxyInvokerMapper;
	private ChannelDispatcher<?> channelDispatcher;
	@Service
	private Logger logger;
	@PostConstruct
	public void init() {
		System.err.println("init");
	}

	@Override
	public void execute(DispatcherContext<Invokers> dispatcherContext) {
		logger.debug("invoker:"+dispatcherContext.getMessage()+":"+dispatcherContext);
		channelDispatcher.response(1111);
	}
	
}
