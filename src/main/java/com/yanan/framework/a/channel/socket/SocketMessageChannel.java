package com.yanan.framework.a.channel.socket;


import java.io.IOException;
import java.io.WriteAbortedException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.yanan.framework.a.channel.buffer.BufferHandleException;
import com.yanan.framework.a.channel.socket.message.Message;
import com.yanan.framework.a.channel.socket.message.MessageProvider;
import com.yanan.framework.a.channel.socket.message.MessageType;
import com.yanan.framework.a.core.AbstractMessageChannelHandler;
import com.yanan.framework.a.core.BufferReady;
import com.yanan.framework.a.core.ByteBufferChannel;
import com.yanan.framework.a.core.ChannelEvent;
import com.yanan.framework.a.core.ChannelEventManger;
import com.yanan.framework.a.core.ChannelEventSource;
import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.MessageChannelListener;
import com.yanan.framework.a.core.MessageChannelMapping;
import com.yanan.framework.a.core.MessageHandler;
import com.yanan.framework.a.core.MessageSerialization;
import com.yanan.framework.plugin.Environment;
import com.yanan.framework.plugin.ProxyModel;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;

/**
 * socket通道实现
 * @author yanan
 * @param <K>
 */
@Register(model = ProxyModel.BOTH)
public class SocketMessageChannel<T> implements MessageChannel<T>,
AbstractMessageChannelHandler<SelectionKey>
,SocketMessageChannelHandler{
	@Service
	private Logger logger;
	@Service
	private MessageSerialization messageSerial;
	@Service
	private SelectorRunningService selectorRunningService;
	public Map<SocketChannel,AbstractMessageChannelHandler<?>> messageChanelHadnler
		= new HashMap<>();
	private ChannelStatus channelStatus = ChannelStatus.CLOSE;
	private int port;
	private String host;
	private ChannelEventSource channelEvent;
	private SocketChannel socketChannel;
	private MessageHandler<T> messageHandler;
	@Service
	private ByteBufferChannel<Message<MessageType>> bufferHandler;
	public MessageSerialization getMessageSerial() {
		return messageSerial;
	}

	public SelectorRunningService getSelectorRunningService() {
		return selectorRunningService;
	}

	public Map<SocketChannel, AbstractMessageChannelHandler<?>> getMessageChanelHadnler() {
		return messageChanelHadnler;
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

	public ChannelEventSource getChannelEvent() {
		return channelEvent;
	}

	public MessageHandler<T> getMessageHandler() {
		return messageHandler;
	}

	public ByteBufferChannel<Message<MessageType>> getBufferHandler() {
		return bufferHandler;
	}

	public MessageChannelMapping<T> getSocketMessageChannelMapping() {
		return socketMessageChannelMapping;
	}

	public MessageProvider getMessageProvider() {
		return messageProvider;
	}

	public BufferReady<Message<MessageType>> getMessageWriteHandler() {
		return messageWriteHandler;
	}

	public ClientType getClientType() {
		return clientType;
	}

	@Service
	public MessageChannelMapping<T> socketMessageChannelMapping; 
	@Service
	private MessageProvider messageProvider;
	private BufferReady<Message<MessageType>> messageWriteHandler;
	private ClientType clientType;
	@PostConstruct
	public void init() {
		channelEvent = new ChannelEventSource(this);
		Environment.getEnviroment().registEventListener(channelEvent, event->{
			System.err.println("事件:"+event);
		});
		ChannelEventManger.getInstance().putEventSource(this, channelEvent);
		Environment.getEnviroment().distributeEvent(channelEvent, ChannelEvent.INITING);
		logger.debug("初始化"+(socketChannel == null?"客户端":"服务端"));
		//初始化消息队列
		logger.debug("获取序列化工具"+messageSerial);
		this.port = 4280;
		channelStatus = ChannelStatus.INIT;
		Environment.getEnviroment().distributeEvent(channelEvent, ChannelEvent.INIT);
		
	}
	
	public void initBuffer() {
		this.messageWriteHandler = new BufferReady<Message<MessageType>> (){
			@Override
			public void write(ByteBuffer buffer){
				try {
					writeToChannel(buffer);
				} catch (WriteAbortedException e) {
					throw new BufferHandleException(e);
				}
			}
			@Override
			public void handleRead(ByteBuffer buffer) {
				try {
					while (true) {
						int len = socketChannel.read(buffer);
						if (len == 0)
							break;
						if (len == -1) {
							throw new SocketException("the socket channel is close!");
						}
					}
				} catch (IOException e) {
					throw new BufferHandleException(e);
				}
			}
			public void onMessage(Message<MessageType> message) {
				try {
					messageHandler.onMessage(message.getMessage());
					logger.debug("accept message from '" + socketChannel.getRemoteAddress() + "':" + message);
				} catch (IOException e) {
					throw new BufferHandleException(e);
				}
			}
		};
		bufferHandler.setBufferReady(messageWriteHandler);
	}
	@Override
	public String toString() {
		return "SocketMessageChannel [logger=" + logger + ", messageSerial=" + messageSerial + ", channelStatus="
				+ channelStatus + ", port=" + port + ", host=" + host + ", channelEvent=" + channelEvent + "]";
	}
	@Override
	public void close() {
		logger.debug("关闭");
		channelStatus = ChannelStatus.CLOSE;
		try {
			LockSupports.removeLockThread(socketChannel);
			socketMessageChannelMapping.remove(this.socketChannel.getRemoteAddress().toString());
			socketChannel.close();
			selectorRunningService.removeChannel(socketChannel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void open() {
		clientType = this.socketChannel == null?ClientType.Consumer:ClientType.Server;
		logger.debug("打开通道"+(clientType==ClientType.Consumer?"客户端":"服务端"));
		logger.debug("远程配置信息");
		if(clientType==ClientType.Consumer) {
			openClient();
		}else {
			openServer();
		}
		try {
			socketMessageChannelMapping.put(this.socketChannel.getRemoteAddress().toString(), this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//初始化读写的bytebuffer  
		initBuffer();
		channelStatus = ChannelStatus.OPEN;
	}
	private void openServer() {
		System.err.println(this.socketChannel);
		selectorRunningService.registerChannel(socketChannel);
		System.err.println(socketMessageChannelMapping);
	}

	private void openClient() {
			try {
				//客户端逻辑
				socketChannel = SocketChannel.open();
				logger.debug("client port:"+this.port);
				socketChannel.configureBlocking(false);
				socketChannel.connect(new InetSocketAddress("127.0.0.1",this.port));
				socketChannel.register(selectorRunningService.getSelector(), SelectionKey.OP_CONNECT);
				this.setSocketChannel(socketChannel);
				selectorRunningService.registerChannel(socketChannel);
				LockSupports.lockAndCatch(socketChannel);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	}

	@Override
	public void transport(T message) {
		logger.debug("发送："+message);
		//获取数据类型
		String messageType = message.getClass().getName();
		logger.debug("消息类型:"+messageType);
		logger.debug("序列化工具:"+bufferHandler);
		Message<MessageType> protocolMessage = messageProvider.request(message);
		//写入队列
		this.bufferHandler.write(protocolMessage);
	}
	@Override
	public void accept(MessageHandler<T> messageHandler) {
		logger.debug("接收："+messageHandler);
		this.messageHandler = messageHandler;
	}
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}
	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}
	@Override
	public void handleRead(SelectionKey key) {
		try {
			logger.debug("read data form "+socketChannel.getRemoteAddress());
			this.bufferHandler.handleRead();
		} catch (Throwable e) {
			key.cancel();
			logger.error("failed to read buffer", e);
			throw new BufferHandleException("failed to read buffer",e);
		}
	}
	protected void writeToChannel(ByteBuffer buffer) throws WriteAbortedException {
		try {
			if(!socketChannel.finishConnect())
				throw new NotYetConnectedException();
			socketChannel.write(buffer);
		} catch (IOException e) {
			throw new WriteAbortedException("ant message write failed!", e);
		}
	}

	@Override
	public void setListener(MessageChannelListener<T> listener) {
		
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setMessageSerial(MessageSerialization messageSerial) {
		this.messageSerial = messageSerial;
	}

	public void setSelectorRunningService(SelectorRunningService selectorRunningService) {
		this.selectorRunningService = selectorRunningService;
	}

	public void setMessageChanelHadnler(Map<SocketChannel, AbstractMessageChannelHandler<?>> messageChanelHadnler) {
		this.messageChanelHadnler = messageChanelHadnler;
	}

	public void setChannelStatus(ChannelStatus channelStatus) {
		this.channelStatus = channelStatus;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setChannelEvent(ChannelEventSource channelEvent) {
		this.channelEvent = channelEvent;
	}

	public void setMessageHandler(MessageHandler<T> messageHandler) {
		this.messageHandler = messageHandler;
	}

	public void setBufferHandler(ByteBufferChannel<Message<MessageType>> bufferHandler) {
		this.bufferHandler = bufferHandler;
	}

	public void setSocketMessageChannelMapping(MessageChannelMapping<T> socketMessageChannelMapping) {
		this.socketMessageChannelMapping = socketMessageChannelMapping;
	}

	public void setMessageProvider(MessageProvider messageProvider) {
		this.messageProvider = messageProvider;
	}

	public void setMessageWriteHandler(BufferReady<Message<MessageType>> messageWriteHandler) {
		this.messageWriteHandler = messageWriteHandler;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}
}
