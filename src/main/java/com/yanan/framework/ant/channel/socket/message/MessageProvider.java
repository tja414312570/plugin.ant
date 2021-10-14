package com.yanan.framework.ant.channel.socket.message;

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
	public Message<SocktMessageType> request(Object content){
		return request(SocktMessageType.REQUEST,content);
	}
	public Message<SocktMessageType> request(SocktMessageType request, Object content) {
		AbstractMessage message = newMessage();
		message.setMessage(content);
		message.setMessageType(request);
		return message;
	}
	public Message<SocktMessageType> exception(Object content){
		return request(SocktMessageType.EXCEPTION,content);
	}
}
