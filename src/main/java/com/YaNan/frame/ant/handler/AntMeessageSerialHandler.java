package com.YaNan.frame.ant.handler;

import java.nio.ByteBuffer;

import com.YaNan.frame.ant.exception.AntMessageSerialException;
import com.YaNan.frame.ant.interfaces.AntMessageSerialization;
import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.utils.SerialUtils;
import com.YaNan.frame.plugin.annotations.Register;
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
		byteBufferOutput = new ByteBufferOutput(buffer,2048);
	}

	public ByteBuffer serialAntMessage(Object serailBean, AntMessagePrototype message) {
		if(serailBean==null)
			return null;
		try {
			Kryo kryo = SerialUtils.getKryo();
			kryo.writeClassAndObject(byteBufferOutput, serailBean);
			byteBufferOutput.flush();
		} catch (KryoException e) {
			throw new AntMessageSerialException("Message body serialization exception", e, message);
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
