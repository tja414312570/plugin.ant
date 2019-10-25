package com.YaNan.frame.ant.test;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.YaNan.frame.ant.exception.AntMessageSerialException;
import com.YaNan.frame.ant.interfaces.AntMessageSerialization;
import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.utils.ByteUtils;

public class SerialTest {
	public static void main(String[] args) {
		PlugsFactory.getInstance().addScanPath("/Users/yanan/eclipse-workspace/plugin.ant/target/classes");
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

		ByteBuffer writeBuffer = ByteBuffer.allocate(20480);
		
		AntMessageSerialization messageSerialization = PlugsFactory.getPlugsInstance(AntMessageSerialization.class);
		Object[] params = new Object[200];
		for(int i = 0;i<params.length;i++) {
			params[i] = i+" data s";
		}
		AntMessagePrototype msg = new AntMessagePrototype();
		msg.setInvokeParmeters(params);
		if(params.length > 250)
			throw new AntMessageSerialException("the max invoke parameter size is 255 , but "+params.length, null);
		//参数个数
		writeBuffer.put((byte) params.length);
		for(int i = 0;i<params.length;i++) {
			byteBuffer = messageSerialization.serialAntMessage(params[i], msg);
			int len = 0;
			if(byteBuffer!=null) {
				byteBuffer.flip();
				len = byteBuffer.remaining();
				writeBuffer.put(ByteUtils.intToByte(len));
				//写入参数内容
				while(byteBuffer != null && byteBuffer.hasRemaining()) {
					byte[] bytes = new byte[1];
					byteBuffer.get(bytes);
					writeBuffer.put(bytes);
				}
				byteBuffer.clear();
			}else {
				writeBuffer.put(ByteUtils.intToByte(len));
			}
			messageSerialization.clear();
		}
		
		System.out.println(new String(writeBuffer.array()));
		//读取模式
		writeBuffer.flip();
		byte[] c = {
				99, 111, 109, 46, 89, 97, 78, 97, 110, 46, 102, 114, 97, 109, 101, 46, 97, 110, 116, 46, 105, 110, 116, 101, 114, 102, 97, 99, 101, 115, 46, 65, 110, 116, 81, 117, 101, 117, 101, 83, 101, 114, 118, 105, 99, 101, 58, 114, 101, 103, 105, 115, 116, 67, 117, 115, 116, 111, 109, 58, 99, 111, 109, 46, 89, 97, 78, 97, 110, 46, 102, 114, 97, 109, 101, 46, 97, 110, 116, 46, 109, 111, 100, 101, 108, 46, 65, 110, 116, 67, 117, 115, 116, 111, 109, 101, 114, 4, 0, 0, 0, 46, 1, 0, 99, 111, 109, 46, 89, 97, 78, 97, 110, 46, 102, 114, 97, 109, 101, 46, 97, 110, 116, 46, 109, 111, 100, 101, 108, 46, 82, 101, 103, 105, 115, 116, 101, 114, 82, 101, 115, 117, 108, -12, -46, 15, -112, 3, 0, 0, 0, 46, 1, 0, 99, 111, 109, 46, 89, 97, 78, 97, 110, 46, 102, 114, 97, 109, 101, 46, 97, 110, 116, 46, 109, 111, 100, 101, 108, 46, 82, 101, 103, 105, 115, 116, 101, 114, 82, 101, 115, 117, 108, -12, -46, 15, -112, 3, 0, 0, 0, 46, 1, 0, 99, 111, 109, 46, 89, 97, 78, 97, 110, 46, 102, 114, 97, 109, 101, 46, 97, 110, 116, 46, 109, 111, 100, 101, 108, 46, 82, 101, 103, 105, 115, 116, 101, 114, 82, 101, 115, 117, 108, -12, -46, 15, -112, 3, 0, 0, 0, 46, 1, 0, 99, 111, 109, 46, 89, 97, 78, 97, 110, 46, 102, 114, 97, 109, 101, 46, 97, 110, 116, 46, 109, 111, 100, 101, 108, 46, 82, 101, 103, 105, 115, 116, 101, 114, 82, 101, 115, 117, 108, -12, -46, 15, -112, 3
				};
		writeBuffer = ByteBuffer.wrap(c);
//		writeBuffer.flip();
		writeBuffer.position(97);
		int nums = writeBuffer.get() & 0xff;
		System.out.println(nums);
		params = new Object[nums];
		byte[] paramLenByte = new byte[4];
		int paramLen = 0;
		int position = writeBuffer.position();
		int limit = 0;
		int recoderLimit = writeBuffer.limit();
		for(int i = 0;i<nums;i++) {
			writeBuffer.limit(recoderLimit);
			System.out.println("资源指针:"+writeBuffer.position()+"资源数量:"+writeBuffer.remaining());
			writeBuffer.get(paramLenByte);
			paramLen = ByteUtils.byteToInt(paramLenByte);
			position+=4;
			limit = position+paramLen;
			System.out.println(paramLen+"  "+position+"  "+limit);
			params[i] = messageSerialization.deserializationAntMessage(writeBuffer,position, limit,String.class );
			position = limit;
			System.out.println(params[i]);
		}
	}
}
