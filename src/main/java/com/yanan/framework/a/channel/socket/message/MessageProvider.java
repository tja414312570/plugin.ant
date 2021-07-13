package com.yanan.framework.a.channel.socket.message;

import java.util.concurrent.atomic.AtomicInteger;

import com.yanan.framework.plugin.annotations.Register;

@Register(signlTon = true)
public class MessageProvider {
	private AtomicInteger atomicInteger = new AtomicInteger();
	public synchronized int getId() {
		if(atomicInteger.get()>=Integer.MAX_VALUE) {
			atomicInteger.set(0);
		}
		return atomicInteger.getAndIncrement();
	}
	public AbstractMessage newMessage() {
		AbstractMessage message = new AbstractMessage();
		message.setId(getId());
		return message;
	}
	public Message<MessageType> request(Object content){
		return request(MessageType.REQUEST,content);
	}
	public Message<MessageType> request(MessageType request, Object content) {
		AbstractMessage message = newMessage();
		message.setMessage(content);
		message.setMessageType(request);
		return message;
	}
	public Message<MessageType> exception(Object content){
		return request(MessageType.EXCEPTION,content);
	}
}
