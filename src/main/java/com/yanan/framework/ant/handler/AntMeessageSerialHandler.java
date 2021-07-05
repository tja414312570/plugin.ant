package com.yanan.framework.ant.handler;

import java.nio.ByteBuffer;

import com.yanan.framework.a.serial.MessageSerialException;
import com.yanan.framework.ant.exception.AntMessageSerialException;
import com.yanan.framework.ant.interfaces.AntMessageSerialization;
import com.yanan.framework.ant.model.AntMessagePrototype;
import com.yanan.framework.ant.utils.SerialUtils;
import com.yanan.framework.plugin.annotations.Register;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

@Register
public class AntMeessageSerialHandler implements AntMessageSerialization{
	private ByteBufferOutput byteBufferOutput;
	
	public void clear() {
		byteBufferOutput.clear();
	}
	
	public AntMeessageSerialHandler() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		byteBufferOutput = new ByteBufferOutput(buffer,2048000);
	}

	public ByteBuffer serialAntMessage(Object serailBean) {
		if(serailBean==null)
			return null;
		try {
			Kryo kryo = SerialUtils.getKryo();
			kryo.writeClassAndObject(byteBufferOutput, serailBean);
			byteBufferOutput.flush();
		} catch (KryoException e) {
			throw new MessageSerialException("Message body serialization exception", e);
		} 
	return byteBufferOutput.getByteBuffer();
	}


	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserializationAntMessage(ByteBuffer byteBuffer, int position, int limit, Class<T> type) {
		Kryo kryo = SerialUtils.getKryo();
		ByteBufferInput input = new ByteBufferInput(byteBuffer);
		input.setPosition(position);
		input.setLimit(limit);
		return (T) kryo.readClassAndObject(input);
	}

}