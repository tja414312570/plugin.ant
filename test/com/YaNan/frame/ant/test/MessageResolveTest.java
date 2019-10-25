package com.YaNan.frame.ant.test;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.YaNan.frame.ant.AntContext;
import com.YaNan.frame.ant.AntContextConfigure;
import com.YaNan.frame.ant.function.queue.AntQueueService;
import com.YaNan.frame.ant.handler.AntMessageHandler;
import com.YaNan.frame.ant.interfaces.BufferReady;
import com.YaNan.frame.ant.model.AntCustomer;
import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.model.RegisterResult;
import com.YaNan.frame.plugin.PlugsFactory;

public class MessageResolveTest {
	static byte[] cache = null;
	public static void main(String[] args) {
		PlugsFactory.getInstance().addScanPath("/Users/yanan/eclipse-workspace/plugin.ant/target/classes");
//		System.out.printlnå("容量计算:"+AntMessageHandler.calculateCapacity(10, 0, 10240));
		AntMessageHandler handler = AntMessageHandler.getHandler(null);
		handler.setBuffer(ByteBuffer.allocate(2024),ByteBuffer.allocate(2));
		AntMessagePrototype message = new AntMessagePrototype();
		message.setRID(1001);
		RegisterResult result = new RegisterResult();
		result.setSid(1001);
		result.setStatus(200);
		message.setInvokeClass(AntProviderService.class);
		try {
			Method method = AntProviderService.class.getDeclaredMethod("registCustom", AntCustomer.class);
			message.setInvokeMethod(method);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		List<Byte> list = new ArrayList<Byte>();
		message.setInvokeParmeters(result,result,result,result);
		handler.write(message,new BufferReady() {
			@Override
			public void bufferReady(ByteBuffer buffer)  {
				buffer.flip();
				byte[] b = new byte[buffer.remaining()];
				buffer.get(b);
				for(int i = 0;i<b.length;i++) {
					list.add(b[i]);
				}
				buffer.flip();
				buffer.clear();
			}
		});
		System.out.println("ok");
		System.out.println(list);
		handler.setWriteBuffer(ByteBuffer.allocate(2048));
		long all = 300000;
		long t = System.currentTimeMillis();
		for(int i = 0 ;i<all;i++)
			handler.write(message,new BufferReady() {
				@Override
				public void bufferReady(ByteBuffer buffer)  {
					buffer.flip();
					cache = new byte[buffer.remaining()];
					buffer.get(cache);
					buffer.flip();
					buffer.clear();
				}
			});
		float times = (System.currentTimeMillis()-t)/1000f; 
		 System.out.println("写吞吐率:"+(all/times)+"\t耗时:"+times+"s \t速度:"+(list.size()*all/1024/1024/times)+"m/s");
		System.out.println("写ok："+handler.getWriteBuffer().position());
		System.out.println(Arrays.toString(handler.getWriteBuffer().array()));
		handler.getWriteBuffer().flip();
		System.out.println("数据长度:"+cache.length);
		handler.setReadBuffer(ByteBuffer.allocate(1024));
		handler.setMaxBufferSize(1024);
		t = System.currentTimeMillis();
		for(int i = 0;i<all;i++) {
//			handler.getReadBuffer().put(b,0,10);
//			System.out.println("剩余:"+handler.getReadBuffer().remaining());
//			handler.handleRead();
//			System.out.println("剩余:"+handler.getReadBuffer().remaining());
			handler.getReadBuffer().put(cache);
			try{
				handler.handleRead();
			}catch (Exception e) {
				e.printStackTrace();
			}
//			
//			handler.getReadBuffer().put(b,50,b.length-50);
//			try{
//				handler.handleRead();
//			}catch (Exception e) {
//				e.printStackTrace();
//			}
//			handler.getReadBuffer().put(b,0,50);
//			try{
//				handler.handleRead();
//			}catch (Exception e) {
//				e.printStackTrace();
//			}
//			handler.getReadBuffer().put(b,50,b.length-50); 
//			try{
//				handler.handleRead();
//			}catch (Exception e) {
//				e.printStackTrace();
//			}
//			System.out.println("剩余:"+handler.getReadBuffer().remaining());
//			System.out.println("数量:"+handler.getMessageNum());
//			AntMessagePrototype getMessage = handler.getMessage();
//			System.out.println(getMessage);
		}
		times = (System.currentTimeMillis()-t)/1000f; 
		System.out.println("吞吐率:"+(all/times)+"\t耗时:"+times+"s \t速度:"+(list.size()*all/1024/1024/times)+"m/s");
		System.out.println(handler.getMessageNum());
		System.out.println("数量:"+handler.getMessageNum());
		AntMessagePrototype getMessage = handler.getMessage();
		try {
			getMessage.decode();
			
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		System.out.println(getMessage);
	}
}
