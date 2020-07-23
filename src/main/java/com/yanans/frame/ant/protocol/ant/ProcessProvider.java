package com.yanan.frame.ant.protocol.ant;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.yanan.frame.ant.abstracts.AbstractProcess;
import com.yanan.frame.ant.handler.AntServiceInstance;
import com.yanan.frame.ant.model.AntMessagePrototype;
import com.yanan.frame.ant.utils.MessageProcesser;

/**
 * 提供抽象Process对象池化
 * @author yanan
 *
 */
public class ProcessProvider {
	private static Map<Integer, AbstractProcess> processMap = new ConcurrentHashMap<Integer, AbstractProcess>();
	private static Queue<AbstractProcess> processPools = new ConcurrentLinkedQueue<AbstractProcess>();
	/**
	 * 获取通道处理
	 * @param handler
	 * @param type
	 * @param key
	 * @return
	 */
	public static AbstractProcess requireChannerlProcess(AntClientService clientService, SocketChannel socketChannel,
			int type, SelectionKey key) {
		int hash = socketChannel.hashCode()+type ;
		AntChannelProcess channelProcess = (AntChannelProcess) processMap.get(hash);
		if(channelProcess == null) {
			synchronized (clientService) {
				if(channelProcess == null) {
					channelProcess =new AntChannelProcess(clientService,socketChannel,type,key);
					processMap.put(hash, channelProcess);
					return channelProcess;
				}
			}
		}
		channelProcess.setKey(key);
		return channelProcess;
	}
	/**
	 * 获取消息处理
	 * @param message
	 * @param handler
	 * @return
	 */
	public static MessageProcesser get(AntMessagePrototype message, AntServiceInstance handler) {
		MessageProcesser messageProcesser = (MessageProcesser) processPools.poll();
		if(messageProcesser == null) {
			messageProcesser = new MessageProcesser();
		}
		messageProcesser.setClientHandler(handler);
		messageProcesser.setMessage(message);
		return messageProcesser;
	}
	/**
	 * 释放消息处理
	 * @param messageProcesser
	 */
	public static void release(MessageProcesser messageProcesser) {
		messageProcesser.setClientHandler(null);
		messageProcesser.setMessage(null);
		processPools.add(messageProcesser);
	}
}