package com.yanan.frame.ant.model;

import com.yanan.frame.ant.type.MessageType;

public class AntResponse extends AntMessagePrototype{
	public AntResponse() {
		this.type = MessageType.RESPONSE;
	}
}