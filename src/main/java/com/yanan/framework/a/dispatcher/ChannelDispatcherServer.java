package com.yanan.framework.a.dispatcher;

import org.slf4j.Logger;

import com.yanan.framework.a.channel.socket.LockSupports;
import com.yanan.framework.a.channel.socket.server.MessageChannelCreateListener;
import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.cluster.ChannelManager;
import com.yanan.framework.a.core.server.ServerMessageChannel;
import com.yanan.framework.a.proxy.Invoker;
import com.yanan.framework.ant.type.MessageType;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.utils.CacheHashMap;
import com.yanan.utils.asserts.Assert;

@Register
public class ChannelDispatcherServer<K> implements ChannelDispatcher<K>{
	
	private CacheHashMap<K, MessageChannel<MessagePrototype<?>>> messagelChannel = new CacheHashMap<>();
	private ThreadLocal<DispatcherContext<?>> dispatcherContextLocal = new InheritableThreadLocal<>();
	private ChannelManager<?> channelManager;
	private Invoker<DefaultDispatcherContext<Object>> invoker;
	@Service
	private Logger logger;
	@Override
	public void bind(ChannelManager<?> server) {
		this.channelManager = server;
		logger.debug("绑定通道管理:"+server);
		if(server.getServerChannel() != null) {
			ServerMessageChannel<MessagePrototype<?>> serverMessageChannel = server.getServerChannel();
			logger.debug("绑定通道消息监听:"+serverMessageChannel);
			serverMessageChannel.onChannelCreate(new MessageChannelCreateListener<MessagePrototype<?>>() {
				@Override
				public void onCreate(MessageChannel<MessagePrototype<?>> messageChannel) {
					messageChannel.accept(message->{
						Assert.isNotNull(invoker);
						DefaultDispatcherContext<Object> dispatcherContext = new DefaultDispatcherContext<>();
						dispatcherContext.setMessageChannel(messageChannel);
						dispatcherContext.setMessagePrototype(message);
						dispatcherContextLocal.set(dispatcherContext);
						if(message.getType()==MessageType.REQUEST) {
							logger.debug("调用信息:"+message);
							invoker.execute(dispatcherContext);
						}else {
							logger.debug("响应信息:"+message);
						}
					});				
				}
			});
		}
	}

	@Override
	public Object request(K channel,Object message) {
		MessageChannel<MessagePrototype<?>> messageChannel = getChannel(channel);
		logger.debug("请求数据:"+messageChannel);
		Request<Object> request = new Request<Object>();
		request.setInvoker(message);
		messageChannel.transport(request);
		//加锁
		LockSupports.lock(message);
		logger.debug("返回数据:"+LockSupports.get(message, message));
		return LockSupports.get(message, message);
	}

	private MessageChannel<MessagePrototype<?>> getChannel(K channel) {
		MessageChannel<MessagePrototype<?>> messageChannel = messagelChannel.get(channel);
		if(messageChannel == null) {
			final MessageChannel<MessagePrototype<?>> newMessageChannel = channelManager.getChannel(channel);
			newMessageChannel.accept(message->{
				Assert.isNotNull(invoker);
				DefaultDispatcherContext<Object> dispatcherContext = new DefaultDispatcherContext<>();
				dispatcherContext.setMessageChannel(newMessageChannel);
				dispatcherContext.setMessagePrototype(message);
				dispatcherContextLocal.set(dispatcherContext);
				logger.debug("调用信息:"+message);
				invoker.execute(dispatcherContext);
				logger.debug("调用信息:"+message);
				invoker.execute(dispatcherContext);
			});
			messageChannel = newMessageChannel;
			messagelChannel.puts(channel, messageChannel);
		}
		return messageChannel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void bind(Invoker<?> invoker) {
		this.invoker = (Invoker<DefaultDispatcherContext<Object>>) invoker;
	}

	@Override
	public void requestAsync(K channel, Object request, Callback callBack) {
		
	}

}
