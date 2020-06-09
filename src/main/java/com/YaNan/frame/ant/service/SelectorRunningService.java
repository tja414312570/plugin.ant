package com.YaNan.frame.ant.service;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.implement.AntChannelProcess;

/**
 * Selector运行时服务
 * @author yanan
 *
 */
public class SelectorRunningService implements Runnable{
	private AntRuntimeService runtimeService;
	private static Logger logger = LoggerFactory.getLogger(AntRuntimeService.class);
	private Selector selector;
	
	public Selector getSelector() {
		return selector;
	}
	public SelectorRunningService(AntRuntimeService antRuntimeService) {
		runtimeService  = antRuntimeService;
		//创建选择器
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	@Override
	public void run() {
		SocketChannel socketChannel;
		AntClientHandler handler;
			  while(true) {
				  try {
						if(selector.select(50) == 0) {//强制50ms执行一次，因为需要先select后register，造成死锁
							continue;
						}
						Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
			            while (keyIter.hasNext()) {
			                SelectionKey key = keyIter.next();
			                //客户端连接到此设备
			                if (key.isAcceptable()){  
			                	logger.debug("accept a service connection!");
			                	socketChannel = ((ServerSocketChannel)key.channel()).accept();
			                	logger.debug("service connection ip:"+socketChannel.getRemoteAddress());
			        	        socketChannel.configureBlocking(false);
			        	        socketChannel.register(key.selector(), SelectionKey.OP_READ);
			        	        handler = new AntClientHandler(socketChannel,runtimeService);
			        	        runtimeService.executeProcess(new AntChannelProcess(handler,SelectionKey.OP_ACCEPT,key));
			        	        runtimeService.getAntClientRegisterService().register(handler);
			                	runtimeService.handlerMapping(socketChannel, handler);
			                	keyIter.remove();
			                	continue;
			                }  
			                socketChannel = (SocketChannel) key.channel();
			                //连接到目标
			                if(key.isConnectable()) {
			                	logger.debug("connect to service success:"+socketChannel.getRemoteAddress());
			                	handler = new AntClientHandler(socketChannel,runtimeService);
			                	key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			                	runtimeService.handlerMapping(socketChannel, handler);
			                	runtimeService.executeProcess(new AntChannelProcess(handler,SelectionKey.OP_CONNECT,key));
			                }
			                handler = runtimeService.getHandler(socketChannel);
			                if(handler == null) {
			                	socketChannel.close();
			                }
			                //可读
			                if(key.isReadable()) {
			                	runtimeService.executeProcess(new AntChannelProcess(handler,SelectionKey.OP_READ,key));
//			                	handler.handleRead(key);
			                }
			                //可写
			                if(key.isWritable()) {
//			                	handler.handlWrite(key);
			                }
			                keyIter.remove(); 
			            }
				} catch (IOException e) {
					e.printStackTrace();
				}
			 
		}
	}
	
}