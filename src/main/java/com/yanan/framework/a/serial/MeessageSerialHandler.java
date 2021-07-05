package com.yanan.framework.a.serial;

import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.yanan.framework.a.core.MessageSerialization;
import com.yanan.framework.ant.utils.SerialUtils;
import com.yanan.framework.plugin.annotations.Register;

@Register(priority = Integer.MAX_VALUE)
public class MeessageSerialHandler implements MessageSerialization{
	private ByteBufferOutput byteBufferOutput;
	
	public void clear() {
		byteBufferOutput.clear();
	}
	
	public MeessageSerialHandler() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		byteBufferOutput = new ByteBufferOutput(buffer,2048000);
	}

	@Override
	public ByteBuffer serial(Object serailBean) {
		if(serailBean==null)
			return null;
		try {
			try {
				byteBufferOutput.clear();;
				Kryo kryo = SerialUtils.getKryo();
				kryo.writeClassAndObject(byteBufferOutput, serailBean);
				byteBufferOutput.flush();
			} catch (KryoException e) {
				throw new MessageSerialException("Message body serialization exception ["+serailBean+"]", e);
			} 
			return byteBufferOutput.getByteBuffer();
		}finally {
			 byteBufferOutput.getByteBuffer().flip();
		}
		
	}


	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserial(ByteBuffer byteBuffer, int position, int limit, Class<T> type) {
		Kryo kryo = SerialUtils.getKryo();
//		kryo.register(type);
		ByteBufferInput input = new ByteBufferInput(byteBuffer);
		input.setPosition(position);
		input.setLimit(limit);
		return (T) kryo.readClassAndObject(input);
	}

}