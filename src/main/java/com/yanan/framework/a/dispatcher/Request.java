package com.yanan.framework.a.dispatcher;

import com.yanan.framework.ant.type.MessageType;

public class Request extends MessagePrototype{
	public Request() {
		this.type = MessageType.REQUEST;
	}
}