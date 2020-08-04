package com.yanan.framework.ant.model;

import com.yanan.framework.ant.type.MessageType;

public class AntRequest extends AntMessagePrototype{
	public AntRequest() {
		this.type = MessageType.REQUEST;
	}
}