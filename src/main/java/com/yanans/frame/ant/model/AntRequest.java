package com.yanan.frame.ant.model;

import com.yanan.frame.ant.type.MessageType;

public class AntRequest extends AntMessagePrototype{
	public AntRequest() {
		this.type = MessageType.REQUEST;
	}
}