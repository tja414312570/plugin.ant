package com.yanan.framework.a.dispatcher;

import com.yanan.framework.ant.type.MessageType;

public class Request<T> extends MessagePrototype<T>{
	public Request() {
		this.type = MessageType.REQUEST;
	}

	@Override
	public String toString() {
		return "Request [RID=" + RID + ", type=" + type + ", invoker=" + invoker + ", timeout=" + timeout + "]";
	}
}