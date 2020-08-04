package com.yanan.framework.ant.interfaces;

import java.io.WriteAbortedException;
import java.nio.ByteBuffer;

public interface BufferReady {
	void bufferReady(ByteBuffer buffer) throws WriteAbortedException;
}