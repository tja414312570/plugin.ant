package com.yanan.framework.a.channel.socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;

import com.yanan.framework.a.channel.socket.message.Message;
import com.yanan.framework.a.channel.socket.message.MessageProvider;
import com.yanan.framework.a.channel.socket.message.MessageType;
import com.yanan.framework.a.core.AbstractMessageChannelHandler;
import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.server.ServerMessageChannelLinstener;
import com.yanan.framework.a.process.AbstractProcess;
import com.yanan.framework.a.process.ExecutorServer;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.utils.reflect.TypeToken;

@Register
public class SocketChannelProcess extends AbstractProcess {
	private SelectionKey key;
	private int ops;
	private SocketChannel socketChannel;
	@Service
	private MessageProvider messageProvider;
	@Service
	private ExecutorServer executorServer;
	
	@Service
	private SocketChannelMappingManager<AbstractMessageChannelHandler<SelectionKey>> socketMapping;
	public void setKey(SelectionKey key) {
		this.key = key;
	}
	@Service
	private Logger logger;

	public SocketChannelProcess(SocketChannel socketChannel, int ops,
			SelectionKey key) {
		this.socketChannel = socketChannel;
		this.ops = ops;
		this.key = key;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		AbstractMessageChannelHandler<SelectionKey> messageChannel = null;
		if(key.attachment()==null) {
			synchronized (key) {
				if(key.attachment()==null) {
					key.attach(true);
					try {
						if (ops == SelectionKey.OP_READ) {
							messageChannel = socketMapping.getMapping(socketChannel);
							if (messageChannel == null) {
								socketChannel.close();
								return;
							}
							messageChannel.handleRead(key);
						} else if (ops == SelectionKey.OP_WRITE) {

						} else if (ops == SelectionKey.OP_CONNECT) {
							try {
								logger.debug("service connection ip:" + socketChannel.getRemoteAddress());
								messageChannel = PlugsFactory.getPluginsInstance(new TypeToken<AbstractMessageChannelHandler<SelectionKey>>() {}.getTypeClass());
								logger.debug("service channel connected:" + messageChannel);
								socketChannel.finishConnect();
								socketMapping.setMapping(socketChannel, messageChannel);
								LockSupports.unLock(socketChannel);
							}catch(Exception e) {
								LockSupports.unLockAndThrows(socketChannel, e);
								throw e;
							}
							
						} else if (ops == SelectionKey.OP_ACCEPT) {
							logger.debug("client connection ip:" + socketChannel.getRemoteAddress());
							socketChannel.configureBlocking(false);
							socketChannel.register(key.selector(), SelectionKey.OP_READ);
							executorServer.execute(new AbstractProcess() {
								@Override
								public void execute() {
									SocketMessageChannelHandler socketMessageChannelHandler = PlugsFactory.getPluginsInstanceNew(SocketMessageChannelHandler.class);
									socketMessageChannelHandler.setSocketChannel(socketChannel);
									AbstractMessageChannelHandler<SelectionKey> messageChannel = (AbstractMessageChannelHandler<SelectionKey>) socketMessageChannelHandler;
									((MessageChannel<?>)messageChannel).open();
									socketMapping.setMapping(socketChannel, messageChannel);
									ServerMessageChannelLinstener serverMessageChannelListener = PlugsFactory.getPluginsInstance(ServerMessageChannelLinstener.class);
									serverMessageChannelListener.onChannelCreate((MessageChannel<?>)messageChannel);
									logger.debug("client channel connected:" + messageChannel);
								}
							});
						}
					} catch (Throwable e) {
						logger.error("error to execute process!"+toString(),e);
						messageChannel = socketMapping.getMapping(socketChannel);
						if(messageChannel != null) {
							StringBuffer msg = new StringBuffer(e.getMessage());
							while((e = e.getCause())!= null) {
								msg.append(" cause by ").append(e.getMessage());
							}
							((MessageChannel<Message<MessageType>>)messageChannel).transport(messageProvider.exception(msg));
						}
					}finally {
						key.attach(null);
						if(!key.isValid()) {
							messageChannel = socketMapping.getMapping(socketChannel);
							if(messageChannel != null) {
								((MessageChannel<?>)messageChannel).close();
							}else {
								try {
									socketChannel.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				
			}
		}
		
		
	}


}