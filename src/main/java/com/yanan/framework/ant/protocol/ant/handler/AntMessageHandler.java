package com.yanan.framework.ant.protocol.ant.handler;

import java.nio.ByteBuffer;

import com.yanan.framework.ant.interfaces.AntMessageSerialization;
import com.yanan.framework.ant.interfaces.BufferReady;
import com.yanan.framework.ant.model.AntMessagePrototype;

public interface AntMessageHandler {

	void write(AntMessagePrototype message, BufferReady messageWriteHandler);

	void handleRead();

	AntMessagePrototype getMessage();

	ByteBuffer getReadBuffer();

	AntMessageSerialization getSerailzationHandler();

}
