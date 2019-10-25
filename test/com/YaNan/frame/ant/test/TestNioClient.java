package com.YaNan.frame.ant.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

public class TestNioClient {
	static  boolean inva = true;
	 public static void main(String[] args) throws IOException {
	        SocketChannel socketChannel = SocketChannel.open();
	        socketChannel.configureBlocking(false);
	        if (!socketChannel.connect(new InetSocketAddress("localhost", 4280))){  
	            //不断地轮询连接状态，直到完成连接  
	            while (!socketChannel.finishConnect()){  
	                //在等待连接的时间里，可以执行其他任务，以充分发挥非阻塞IO的异步特性  
	                //这里为了演示该方法的使用，只是一直打印"."  
	                System.out.print(".");    
	            }  
	         }
	       int nums = 2;
	       String send = ("send send send send send send send \n");
	       long  s = System.currentTimeMillis();
	     
	        //为了与后面打印的"."区别开来，这里输出换行符  
	        //分别实例化用来读写的缓冲区  
	       ByteBuffer writeBuf = ByteBuffer.allocate(148);
	        new Thread(new Runnable() {
				
				@Override
				public void run() {
					for(int i = 0;i<nums;i++) {
						writeBuf.put(send.getBytes());
						
						 while (writeBuf.hasRemaining()&&socketChannel.isConnected()) {
					            //如果用来向通道中写数据的缓冲区中还有剩余的字节，则继续将数据写入信道  
					                try {
					                	writeBuf.flip();
					                	System.out.println("写入数量："+send.getBytes().length+"   "+writeBuf.remaining());
										socketChannel.write(writeBuf);
										writeBuf.compact();
										
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}  
					            
					        }
					}
				}
			}).start();
	        ByteBuffer readBuf = ByteBuffer.allocateDirect(1024000);
	        CountDownLatch counter = new CountDownLatch(nums);
	        new Thread(new Runnable() {
				@Override
				public void run() {
			        //如果read（）接收到-1，表明服务端关闭，抛出异常  
			            try {
			            	while(inva) {
								while (socketChannel.read(readBuf) >0){
								    readBuf.flip();
//								    stringBuffer.append(new String(readBuf.array(),0,readBuf.limit()));
								    readBuf.clear();
								    byte[] bytes = new byte[1024];
								    readBuf.get(bytes);
								    for(byte b : bytes) {
								    	if(b == '\n') {
								    		counter.countDown();
								    	}
								    }
								    readBuf.clear();
								    System.out.println(new String(bytes));
								}
			            	}
						} catch (IOException e) {
							e.printStackTrace();
						}  
				}
			}).start();
	        new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(inva) {
						try {
//							System.out.println("计数器："+counter);
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
	        try {
				counter.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        float times = (System.currentTimeMillis()-s)/1000f;
	        inva = false;
	        System.out.println("吞吐率:"+(nums/times)+"\t耗时:"+times+"s \t速度:"+(send.getBytes().length*nums/1024/1024/times)+"m/s");
	      //打印出接收到的数据  
	      
	        //关闭信道  
//	        socketChannel.close(); 
	 }
}
