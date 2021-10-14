package com.yanan.framework.ant.channel.socket.server;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.yanan.framework.ant.channel.socket.ChannelStatus;
import com.yanan.framework.ant.channel.socket.SelectorRunningService;
import com.yanan.framework.ant.core.AbstractMessageChannelHandler;
import com.yanan.framework.ant.core.ChannelEvent;
import com.yanan.framework.ant.core.ChannelEventManger;
import com.yanan.framework.ant.core.MessageChannel;
import com.yanan.framework.ant.core.MessageChannelMapping;
import com.yanan.framework.ant.core.MessageSerialization;
import com.yanan.framework.ant.core.server.ServerChannelEventSource;
import com.yanan.framework.ant.core.server.ServerMessageChannel;
import com.yanan.framework.ant.core.server.ServerMessageChannelLinstener;
import com.yanan.framework.plugin.Environment;
import com.yanan.framework.plugin.ProxyModel;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.framework.plugin.event.InterestedEventSource;

/**
 * socket通道实现
 * @author yanan
 * @param <T>
 */
@Register(model = ProxyModel.BOTH)
public class SocketServerMessageChannel<T> implements ServerMessageChannel<T>,
ServerMessageChannelLinstener{
	@Service
	private Logger logger;
	@Service
	private MessageSerialization messageSerial;
	@Service
	private SelectorRunningService selectorRunningService;
	public Map<SocketChannel,AbstractMessageChannelHandler<?>> messageChanelHadnler
		= new HashMap<>();
	public List<MessageChannel<?>> channelList = new CopyOnWriteArrayList<>();
	@Service
	public MessageChannelMapping<T> socketMessageChannelMapping; 
	private ChannelStatus channelStatus = ChannelStatus.CLOSE;
	private int port;
	private String host;
	private InterestedEventSource channelEvent;
	private MessageChannelCreateListener<T> channelListener;
	private ServerSocketChannel serverSocketChannel;
	@PostConstruct
	public void init() {
		channelEvent = new ServerChannelEventSource(this);
		Environment.getEnviroment().registEventListener(channelEvent, event->{
			System.err.println("事件:"+event);
		});
		ChannelEventManger.getInstance().putEventSource(this, channelEvent);
		Environment.getEnviroment().distributeEvent(channelEvent, ChannelEvent.INITING);
		logger.debug("初始化");
		//初始化消息队列
		logger.debug("获取序列化工具"+messageSerial);
		this.port = 4280;
		channelStatus = ChannelStatus.INIT;
		Environment.getEnviroment().distributeEvent(channelEvent, ChannelEvent.INIT);
		
	}
	public MessageSerialization getMessageSerial() {
		return messageSerial;
	}
	public SelectorRunningService getSelectorRunningService() {
		return selectorRunningService;
	}
	public Map<SocketChannel, AbstractMessageChannelHandler<?>> getMessageChanelHadnler() {
		return messageChanelHadnler;
	}
	public List<MessageChannel<?>> getChannelList() {
		return channelList;
	}
	public MessageChannelMapping<T> getSocketMessageChannelMapping() {
		return socketMessageChannelMapping;
	}
	public ChannelStatus getChannelStatus() {
		return channelStatus;
	}
	public int getPort() {
		return port;
	}
	public String getHost() {
		return host;
	}
	public InterestedEventSource getChannelEvent() {
		return channelEvent;
	}
	public MessageChannelCreateListener<T> getChannelListener() {
		return channelListener;
	}
	public ServerSocketChannel getServerSocketChannel() {
		return serverSocketChannel;
	}
	@Override
	public void close() {
		logger.debug("关闭");
		channelStatus = ChannelStatus.CLOSE;
		channelList.forEach(channel->{
			channel.close();
		});
		selectorRunningService.removeChannel(serverSocketChannel);
	}
	@Override
	public void open() {
		logger.debug("打开通道");
		logger.debug("远程配置信息");
		try {
			serverSocketChannel = ServerSocketChannel.open();
			logger.debug("listening server port:"+this.port);
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(
					new InetSocketAddress(
							Integer.valueOf(port)));
			serverSocketChannel.register(selectorRunningService.getSelector(), SelectionKey.OP_ACCEPT);
			selectorRunningService.registerChannel(serverSocketChannel);
			//初始化读写的bytebuffer  
		} catch (IOException e) {
			throw new RuntimeException("failed to open channel!",e);
		}
		channelStatus = ChannelStatus.OPEN;
	}
	@Override
	public  MessageChannel<T> getMessageChannel(String channelMark) {
		return socketMessageChannelMapping.get(channelMark);
	}
	public void setMessageChannel(String name,MessageChannel<T> messageChannel) {
		socketMessageChannelMapping.put(name, messageChannel);
	}
	@Override
	public List<MessageChannel<T>> getMessageChannelList() {
		List<MessageChannel<T>> list = new ArrayList<>();
		list.addAll(socketMessageChannelMapping.values());
		return list;
	}
	@Override
	public void onChannelCreate(MessageChannelCreateListener<T> channelListener) {
		this.channelListener = channelListener;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void onChannelCreate(MessageChannel<?> messageChannel) {
		System.out.println("新服务连接:"+messageChannel);
		channelList.add(messageChannel);
		if(this.channelListener != null)
			this.channelListener.onCreate((MessageChannel<T>) messageChannel);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <K> K getServerMessageChannel() {
		return (K) this;
	}
	@Override
	public String toString() {
		return "SocketServerMessageChannel [logger=" + logger + ", messageSerial=" + messageSerial
				+ ", selectorRunningService=" + selectorRunningService + ", messageChanelHadnler="
				+ messageChanelHadnler + ", channelList=" + channelList + ", socketMessageChannelMapping="
				+ socketMessageChannelMapping + ", channelStatus=" + channelStatus + ", port=" + port + ", host=" + host
				+ ", channelEvent=" + channelEvent + ", channelListener=" + channelListener + ", serverSocketChannel="
				+ serverSocketChannel + "]";
	}
}
