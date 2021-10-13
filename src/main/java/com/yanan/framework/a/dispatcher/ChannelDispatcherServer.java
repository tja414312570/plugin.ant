package com.yanan.framework.a.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.yanan.framework.a.channel.socket.LockSupports;
import com.yanan.framework.a.channel.socket.server.MessageChannelCreateListener;
import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.cluster.ChannelManager;
import com.yanan.framework.a.core.server.ServerMessageChannel;
import com.yanan.framework.a.proxy.Callback;
import com.yanan.framework.a.proxy.Invoker;
import com.yanan.framework.ant.type.MessageType;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.utils.CacheHashMap;
import com.yanan.utils.reflect.TypeToken;

@Register
public class ChannelDispatcherServer implements ChannelDispatcher{
	
	private CacheHashMap<Object, MessageChannel<MessagePrototype<?>>> messagelChannel = new CacheHashMap<>();
	private Map<Integer,Callback<?>> asyncMap = new ConcurrentHashMap<>();
	private Map<Class<?>, Invoker<DispatcherContext<Object>>> invokerMapping = new HashMap<>();
	private ThreadLocal<DispatcherContext<?>> dispatcherContextLocal = new InheritableThreadLocal<>();
	private ChannelManager<Object> channelManager;
	private AtomicInteger count = new AtomicInteger();
	@Service
	private Logger logger;
	@SuppressWarnings("unchecked")
	@Override
	public <K> void bind(ChannelManager<K> server) {
		this.channelManager = (ChannelManager<Object>) server;
		logger.debug("绑定通道管理:"+server);
		if(server.getServerChannel() != null) {
			ServerMessageChannel<MessagePrototype<?>> serverMessageChannel = server.getServerChannel();
			logger.debug("绑定通道消息监听:"+serverMessageChannel);
			serverMessageChannel.onChannelCreate(new MessageChannelCreateListener<MessagePrototype<?>>() {
				@Override
				public void onCreate(MessageChannel<MessagePrototype<?>> messageChannel) {
					messageChannel.accept(message->{
						doExecute(messageChannel,message);
					});				
				}
			});
		}
	}
	void doExecute(MessageChannel<?> messageChannel, MessagePrototype<?> message) {
		Object invokerMessage = message.getInvoker();
		if(invokerMessage==null) {
			//空请求
			return;
		}
		DefaultDispatcherContext<Object> dispatcherContext = new DefaultDispatcherContext<>();
		dispatcherContext.setMessageChannel(messageChannel);
		dispatcherContext.setMessagePrototype(message);
		dispatcherContextLocal.set(dispatcherContext);
		if(message.getType()==MessageType.REQUEST) {
			logger.debug("调用信息:"+message);
			Class<?> invokerClass = message.getInvoker().getClass();
			logger.debug("消息类"+invokerClass);
			Invoker<DispatcherContext<Object>> invoker = invokerMapping.get(invokerClass);
			if(invoker == null) {
				synchronized (invokerClass) {
					if(invoker == null) {
						invoker = PlugsFactory.getPluginsInstanceByAttributeStrict(new TypeToken<Invoker<DispatcherContext<Object>>>() {}.getTypeClass(), invokerClass.getName());
						invokerMapping.put(invokerClass, invoker);
					}
				}
			}
			invoker.execute(dispatcherContext);
		}else {
			Callback<?> callBack = asyncMap.get(message.getRID());
			if(callBack != null) {
				if(message.getType() == MessageType.EXCEPTION) {
					logger.debug("异步回调异常:"+message);
					callBack.failed().onException((Exception) message.getInvoker(), dispatcherContext);
				}else {
					logger.debug("异步响应信息:"+message+callBack.success());
					callBack.success().onMessage(message.getInvoker(), dispatcherContext);
				}
			}else {
				if(message.getType() == MessageType.EXCEPTION) {
					logger.debug("异常信息:"+message);
				}else {
					logger.debug("响应信息:"+message);
				}
				LockSupports.set(message.getRID(), message.getRID(), message);
				LockSupports.unLock(message.getRID());
			}
		}
	}
	@Override
	public <K> void requestAsync(K channel, Object message, Callback<?> callBack) {
		MessageChannel<MessagePrototype<?>> messageChannel = getChannel(channel);
		logger.debug("请求数据:"+messageChannel);
		Request<Object> request = new Request<Object>();
		request.setInvoker(message);
		request.setRID(count.getAndIncrement());
		messageChannel.transport(request);
		logger.debug("异步调用:"+request);
		asyncMap.put(request.getRID(), callBack);
		
	}
	@Override
	public <K> Object request(K channel,Object message) {
		MessageChannel<MessagePrototype<?>> messageChannel = getChannel(channel);
		logger.debug("请求数据:"+messageChannel);
		Request<Object> request = new Request<Object>();
		request.setInvoker(message);
		request.setRID(count.getAndIncrement());
		messageChannel.transport(request);
		//加锁
		LockSupports.lock(request.getRID());
		MessagePrototype<Object> messagePrototype = LockSupports.get(request.getRID(), request.getRID());
		logger.debug("返回数据:"+messagePrototype);
		if(messagePrototype.getType()==MessageType.EXCEPTION)
			throw new RuntimeException("remote err message:"+messagePrototype.getInvoker());
		return messagePrototype.getInvoker();
	}

	private <K> MessageChannel<MessagePrototype<?>> getChannel(K channel) {
		MessageChannel<MessagePrototype<?>> messageChannel = messagelChannel.get(channel);
		if(messageChannel == null) {
			final MessageChannel<MessagePrototype<?>> newMessageChannel = channelManager.getChannel(channel);
			newMessageChannel.accept(message->{
				doExecute(newMessageChannel,message);
			});
			messageChannel = newMessageChannel;
			messagelChannel.puts(channel, messageChannel);
		}
		return messageChannel;
	}

	@Override
	public void bind(Invoker<?> invoker) {
//		this.invoker = (Invoker<DefaultDispatcherContext<Object>>) invoker;
	}


}
