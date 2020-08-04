package com.yanan.framework.ant.model;

import com.yanan.framework.ant.type.MessageType;

public class AntResponse extends AntMessagePrototype{
	public AntResponse() {
		this.type = MessageType.RESPONSE;
	}
}