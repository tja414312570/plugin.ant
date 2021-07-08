package com.yanan.framework.a.channel.socket.message;

/**
 * Socket Message Protocol
 * @author YaNan
 *
 */
public class AbstractMessage implements Message<MessageType>{
	
	private Object message;
	private MessageType messageType;
	private int id;

	@SuppressWarnings("unchecked")
	public <T> T getMessage() {
		return (T) message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "AbstractMessage [message=" + message + ", messageType=" + messageType + ", id=" + id + "]";
	}

	@Override
	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}
}
