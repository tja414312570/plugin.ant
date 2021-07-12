package com.yanan.framework.a.dispatcher;

import com.yanan.framework.ant.type.MessageType;

public class AntResponse extends AntMessagePrototype{
	public AntResponse() {
		this.type = MessageType.RESPONSE;
	}
}