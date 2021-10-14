package com.yanan.framework.ant.channel.socket;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.yanan.framework.ant.process.AbstractProcess;
import com.yanan.framework.plugin.PlugsFactory;

/**
 * 提供抽象Process对象池化
 * @author yanan
 *
 */
public class ProcessProvider {
	private static Map<Integer, AbstractProcess> processMap = new ConcurrentHashMap<Integer, AbstractProcess>();
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
	
}