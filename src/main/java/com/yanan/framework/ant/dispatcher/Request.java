package com.yanan.framework.ant.dispatcher;

import com.yanan.framework.ant.type.MessageType;

public class Request<T> extends MessagePrototype<T>{
	public Request() {
		this.type = MessageType.REQUEST;
	}

	@Override
	public String toString() {
		return "Request [RID=" + RID + ", type=" + MessageType.getType(type) + ", invoker=" + invoker + ", timeout=" + timeout + "]";
	}
}