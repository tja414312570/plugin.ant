package com.yanan.framework.ant.channel.socket.message;

/**
 * Socket Message Protocol
 * @author YaNan
 *
 */
public class AbstractMessage implements Message<SocktMessageType>{
	
	private Object message;
	private SocktMessageType messageType;
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
	public SocktMessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(SocktMessageType messageType) {
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
