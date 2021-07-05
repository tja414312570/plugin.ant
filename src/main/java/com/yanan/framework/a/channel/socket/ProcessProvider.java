package com.yanan.framework.a.channel.socket;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.yanan.framework.a.channel.buffer.MessageProcesser;
import com.yanan.framework.a.process.AbstractProcess;
import com.yanan.framework.ant.handler.AntServiceInstance;
import com.yanan.framework.ant.model.AntMessagePrototype;
import com.yanan.framework.plugin.PlugsFactory;

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
	public static AbstractProcess requireChannerlProcess(SocketChannel socketChannel,
			int type, SelectionKey key) {
		int hash = socketChannel.hashCode()+type ;
		SocketChannelProcess channelProcess = (SocketChannelProcess) processMap.get(hash);
		if(channelProcess == null) {
			synchronized (socketChannel) {
				if(channelProcess == null) {
					channelProcess = PlugsFactory.getPluginsInstance(SocketChannelProcess.class,socketChannel,type,key);
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