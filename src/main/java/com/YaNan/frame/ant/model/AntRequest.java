package com.YaNan.frame.ant.model;

import com.YaNan.frame.ant.type.MessageType;

public class AntRequest extends AntMessagePrototype{
	public AntRequest() {
		this.type = MessageType.REQUEST;
	}
}
