package com.YaNan.frame.ant.test;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.model.RegisterResult;
import com.YaNan.frame.ant.utils.SerialUtils;
import com.YaNan.frame.plugin.PlugsFactory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;

public class KyroTest {
	public static void main(String[] args) {
		PlugsFactory.getInstance().addScanPath("/Users/yanan/eclipse-workspace/plugin.ant.queen/target/classes");
		Kryo kryo = SerialUtils.getKryo();
		RegisterResult result = new RegisterResult();
		result.setSid(1001);
		result.setStatus(200);
		RegisterResult result2 = new RegisterResult();
		result.setSid(1002);
		result.setStatus(300);
		Object[] params = new Object[2];
		params[0] = result;
		params[1] = result2;
		ByteBuffer byteBuffer = ByteBuffer.allocate(12);
		ByteBufferOutput bbos = new ByteBufferOutput(byteBuffer,2048);
		kryo.writeClassAndObject(bbos, params);
		bbos.flush();
		System.out.println(Arrays.toString(bbos.getByteBuffer().array()));
		
		Input input = new Input(new ByteArrayInputStream(bbos.getByteBuffer().array()));
		Object obj = kryo.readClassAndObject(input);
		System.out.println(obj);
		
		
		AntMessagePrototype msg = new AntMessagePrototype();
		msg.setBuffered(byteBuffer.array());
		try {
			msg.decode();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
