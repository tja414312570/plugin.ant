package com.yanan.framework.ant.dispatcher;

import com.yanan.framework.ant.type.MessageType;

public class Response<T> extends MessagePrototype<T>{
	public Response() {
		this.type = MessageType.RESPONSE;
	}

	@Override
	public String toString() {
		return "Response [RID=" + RID + ", type=" + type + ", invoker=" + invoker + ", timeout=" + timeout + "]";
	}
}