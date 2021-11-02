package com.yanan.framework.ant.dispatcher;

import com.yanan.framework.ant.core.MessageChannel;
import com.yanan.framework.ant.type.MessageType;

public class DefaultDispatcherContext<T> implements DispatcherContext<T>{
	protected MessageChannel<Object> messageChannel;
	protected long requestTime;
	protected T message;
	protected ChannelDispatcher channelDispatcher;
	protected MessagePrototype<Object> messagePrototype;

	public MessagePrototype<?> getMessagePrototype() {
		return messagePrototype;
	}

	@SuppressWarnings("unchecked")
	public void setMessagePrototype(MessagePrototype<?> messagePrototype) {
		this.messagePrototype = (MessagePrototype<Object>) messagePrototype;
		this.message = (T) messagePrototype.getInvoker();
	}
	public MessageChannel<?> getMessageChannel() {
		return messageChannel;
	}
	@SuppressWarnings("unchecked")
	public void setMessageChannel(MessageChannel<?> messageChannel) {
		this.messageChannel = (MessageChannel<Object>) messageChannel;
	}
	public Long getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Long requestTime) {
		this.requestTime = requestTime;
	}
	public T getMessage() {
		return message;
	}
	public void setMessage(T message) {
		this.message = message;
	}
	public ChannelDispatcher getChannelDispatcher() {
		return channelDispatcher;
	}
	public void setChannelDispatcher(ChannelDispatcher channelDispatcher) {
		this.channelDispatcher = channelDispatcher;
	}

	@Override
	public void response(Object message) {
		messagePrototype.setInvoker(message);
		messagePrototype.setType(MessageType.RESPONSE);
		messageChannel.transport(messagePrototype);
	}

	@Override
	public void exception(Throwable t) {
		messagePrototype.setInvoker(t);
		messagePrototype.setType(MessageType.EXCEPTION);
		messageChannel.transport(messagePrototype);
	};
}
