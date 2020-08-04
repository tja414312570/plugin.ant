package com.yanan.framework.ant.protocol.ant;

import java.io.IOException;
import java.io.WriteAbortedException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.framework.ant.abstracts.AbstractProcess;
import com.yanan.framework.ant.abstracts.ClientInstance;
import com.yanan.framework.ant.exception.AntInitException;
import com.yanan.framework.ant.exception.AntMessageResolveException;
import com.yanan.framework.ant.handler.AntServiceInstance;
import com.yanan.framework.ant.interfaces.AntMessageSerialization;
import com.yanan.framework.ant.interfaces.BufferReady;
import com.yanan.framework.ant.model.AntMessagePrototype;
import com.yanan.framework.ant.model.AntProviderSummary;
import com.yanan.framework.ant.protocol.ant.handler.AntMessageHandler;
import com.yanan.framework.ant.service.AntRuntimeService;
import com.yanan.framework.ant.type.ClientType;
import com.yanan.framework.ant.type.MessageType;

/**
 * Ant协议连接实例
 * @author yanan
 *
 */
public class AntClientInstance implements ClientInstance {
	private BufferReady messageWriteHandler;
	private SocketAddress remoteAddress;
	private static Logger logger = LoggerFactory.getLogger(AntClientInstance.class);
	private Throwable closeCause;
	private AntServiceInstance serviceInstance;
	private SocketChannel socketChannel;
	private AntRuntimeService runtimeService;
	private AntClientService clientService;
	private AntMessageHandler messageHandler;

	public AntClientInstance(AntClientService clientService, SocketChannel socketChannel,
			AntProviderSummary providerSummary) {
		this.clientService = clientService;
		this.runtimeService = clientService.getRuntimeService();
		this.messageWriteHandler = new BufferReady() {
			@Override
			public void bufferReady(ByteBuffer buffer) throws WriteAbortedException {
				writeToChannel(buffer);
			}
		};
		this.socketChannel = socketChannel;
		this.serviceInstance = new AntServiceInstance(runtimeService, this);
		this.serviceInstance.setAttribute(AntProviderSummary.class, providerSummary);
		this.serviceInstance.setAttribute(AntClientInstance.class, this);
		try {
			this.remoteAddress = this.socketChannel.getRemoteAddress();
		} catch (IOException e) {
			logger.error("failed to get socket address!", e);
		}
		this.messageHandler = new AntMessageHandler(runtimeService);
	}

	@Override
	public void write(AntMessagePrototype message) {
		logger.debug("write message to '" + this.getRemoteAddress() + "':" + message);
		this.messageHandler.write(message, this.messageWriteHandler);
	}

	/**
	 * 将缓冲区数据写入到通道
	 * 
	 * @param buffer
	 * @throws WriteAbortedException
	 */
	protected void writeToChannel(ByteBuffer buffer) throws WriteAbortedException {
		try {
			if(!socketChannel.finishConnect())
				throw new NotYetConnectedException();
			buffer.flip();
			socketChannel.write(buffer);
			buffer.compact();
		} catch (IOException e) {
			runtimeService.tryRecoveryServiceAndNotifyDiscoveryService(this.serviceInstance);
			throw new WriteAbortedException("ant message write failed!", e);
		}
		buffer.flip();
		buffer.clear();
	}

	public String getRemoteAddress() {
		return this.remoteAddress.toString();
	}

	public void handleRead(SelectionKey key) {
		try {
			while (true) {
				int len = socketChannel.read(this.messageHandler.getReadBuffer());
				if (len == 0)
					break;
				if (len == -1) {
					throw new SocketException("the socket channel is close!");
				}
			}
			this.messageHandler.handleRead();
			AntMessagePrototype message;
			while ((message = messageHandler.getMessage()) != null) {
				logger.debug("accept message from '" + this.getRemoteAddress() + "':" + message);
				this.serviceInstance.onMessage(message);
			}
		} catch (AntMessageResolveException amre) {
			logger.error("failed to read buffer", amre);
			if (serviceInstance.getClientType() == ClientType.Provider) {
				AntMessagePrototype amp = new AntMessagePrototype();
				amp.setType(MessageType.EXCEPTION);
				amp.setRID(amre.getRID());
				amp.setTimeout(runtimeService.getContextConfigure().getTimeout());
				amp.setInvokeClass(AntMessageResolveException.class);
				amp.setInvokeParmeters(amre.getMessage());
				this.write(amp);
			}
			key.cancel();
		} catch (Throwable e) {
			key.cancel();
			logger.error("failed to read buffer", e);
			this.close(e);
			runtimeService.executeProcess(new AbstractProcess() {
				@Override
				public void execute() {
					runtimeService.tryRecoveryServiceAndNotifyDiscoveryService(serviceInstance);
				}
			});
		}
	}

	/**
	 * 关闭服务
	 * 
	 * @param cause 关闭原因
	 */
	public void close(Throwable cause) {
		try {
			if (this.closeCause != null)
				return;
			logger.debug("the socket channel close，info :" + socketChannel.getRemoteAddress().toString() + "\n cause:"
					+ cause.getMessage(), cause);
			this.closeCause = cause;
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("an error has occurred when close socket channel", e);
		}
	}

	@Override
	public AntServiceInstance getServiceInstance() {
		return this.serviceInstance;
	}

	@Override
	public AntMessageSerialization getSerailzationHandler() {
		return this.messageHandler.getSerailzationHandler();
	}

	@Override
	public AntRuntimeService getRuntimeService() {
		return this.runtimeService;
	}

	public AntClientService getClientService() {
		return clientService;
	}
	public void registTimeout() {
		this.close(new AntInitException("regist timeout for "+this.serviceInstance.getId()));
	}
	public void connectTimeout() {
		this.close(new AntInitException("connect timeout"));
	}
}