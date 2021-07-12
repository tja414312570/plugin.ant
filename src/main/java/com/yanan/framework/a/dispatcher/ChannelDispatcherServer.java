package com.yanan.framework.a.dispatcher;

import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.MessageHandler;
import com.yanan.framework.a.core.cluster.ChannelManager;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.utils.CacheHashMap;
import com.yanan.utils.asserts.Assert;

@Register
public class ChannelDispatcherServer<K> implements ChannelDispatcher<K>,MessageHandler<MessagePrototype<?>>{
	
	private CacheHashMap<K, MessageChannel<MessagePrototype<?>>> messagelChannel = new CacheHashMap<>();
	private ChannelManager<?> channelManager;
	private Invoker<?> invoker;
	@Override
	public void bind(ChannelManager<?> server) {
		this.channelManager = server;
	}

	@Override
	public Object request(K channel,Object message) {
		MessageChannel<MessagePrototype<?>> messageChannel = getChannel(channel);
		System.err.println("通道:"+messageChannel);
		Request request = new Request();
		messageChannel.transport(request);
		return "ok!";
	}

	private MessageChannel<MessagePrototype<?>> getChannel(K channel) {
		MessageChannel<MessagePrototype<?>> messageChannel = messagelChannel.get(channel);
		if(messageChannel == null) {
			messageChannel = channelManager.getChannel(channel);
			messageChannel.accept(this);
		}
		return messageChannel;
	}

	@Override
	public void onMessage(MessagePrototype<?> message) {
		Assert.isNotNull(invoker);
		invoker.invoke(message.getInvoker());
	}

	@Override
	public void bind(Invoker<?> invoker) {
		invoker.bind(this);
		this.invoker = invoker;
	}

}
