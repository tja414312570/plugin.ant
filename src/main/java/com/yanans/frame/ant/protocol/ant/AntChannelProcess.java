package com.yanan.frame.ant.protocol.ant;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.ant.abstracts.AbstractProcess;
import com.yanan.frame.ant.model.AntProviderSummary;
import com.yanan.frame.ant.protocol.ant.handler.AntRegisterHandler;
import com.yanan.frame.ant.utils.ObjectLock;

public class AntChannelProcess extends AbstractProcess {
	private SelectionKey key;
	private int ops;
	private AntClientService antClientService;
	private SocketChannel socketChannel;

	public void setKey(SelectionKey key) {
		this.key = key;
	}

	private static Logger logger = LoggerFactory.getLogger(AntChannelProcess.class);

	public AntChannelProcess(AntClientService antClientService, SocketChannel socketChannel, int ops,
			SelectionKey key) {
		this.antClientService = antClientService;
		this.socketChannel = socketChannel;
		this.ops = ops;
		this.key = key;
	}

	@Override
	public void execute() {
		AntClientInstance clientInstance;
		try {
			if (ops == SelectionKey.OP_READ) {
				clientInstance = (AntClientInstance) antClientService.getHandler(socketChannel);
				if (clientInstance == null) {
					socketChannel.close();
				}
				clientInstance.handleRead(key);
			} else if (ops == SelectionKey.OP_WRITE) {

			} else if (ops == SelectionKey.OP_CONNECT) {
				AntProviderSummary antProviderSummary = (AntProviderSummary) key.attachment();
				clientInstance = new AntClientInstance(antClientService, socketChannel,
						antProviderSummary);
				antClientService.handler(socketChannel, clientInstance);
				logger.debug("Socket channel connected:" + clientInstance);
				AntRegisterHandler registerHandler = new AntRegisterHandler(clientInstance);
				antClientService.getAntRegisterServcie().register(registerHandler);
			} else if (ops == SelectionKey.OP_ACCEPT) {
				logger.debug("service connection ip:" + socketChannel.getRemoteAddress());
				socketChannel.configureBlocking(false);
				socketChannel.register(key.selector(), SelectionKey.OP_READ);
				clientInstance = new AntClientInstance(antClientService, socketChannel, null);
				antClientService.getAntClientRegisterService()
						.register(clientInstance);
				antClientService.handler(socketChannel, clientInstance);
			}
		} catch (Throwable e) {
			logger.debug("error to execute process!"+toString(),e);
			AntProviderSummary summary = (AntProviderSummary) key.attachment();
			if (summary != null) {
				ObjectLock.getLock(summary.getName()).release();
			}
		}
	}

	@Override
	public String toString() {
		return "AntChannelProcess [key=" + key + ", ops=" + ops + ", antClientService=" + antClientService
				+ ", socketChannel=" + socketChannel + "]";
	}

}