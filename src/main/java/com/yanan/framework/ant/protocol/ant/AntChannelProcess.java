package com.yanan.framework.ant.protocol.ant;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.framework.ant.abstracts.AbstractProcess;
import com.yanan.framework.ant.abstracts.ClientInstance;
import com.yanan.framework.ant.abstracts.ClientService;
import com.yanan.framework.ant.model.AntProviderSummary;
import com.yanan.framework.ant.protocol.ant.handler.AntRegisterHandler;
import com.yanan.framework.ant.utils.ObjectLock;
import com.yanan.framework.plugin.Environment;
import com.yanan.framework.plugin.PlugsFactory;

public class AntChannelProcess extends AbstractProcess {
	private SelectionKey key;
	private int ops;
	private ClientService clientService;
	private SocketChannel socketChannel;

	public void setKey(SelectionKey key) {
		this.key = key;
	}

	private static Logger logger = LoggerFactory.getLogger(AntChannelProcess.class);

	public AntChannelProcess(ClientService clientService, SocketChannel socketChannel, int ops,
			SelectionKey key) {
		this.clientService = clientService;
		this.socketChannel = socketChannel;
		this.ops = ops;
		this.key = key;
	}

	@Override
	public void execute() {
		ClientInstance clientInstance;
		try {
			if (ops == SelectionKey.OP_READ) {
				clientInstance = clientService.getClientHandler(socketChannel);
				if (clientInstance == null) {
					socketChannel.close();
				}
				clientInstance.handleRead(key);
			} else if (ops == SelectionKey.OP_WRITE) {

			} else if (ops == SelectionKey.OP_CONNECT) {
				Environment.getEnviroment().distributeEvent(null, null);
				AntProviderSummary antProviderSummary = (AntProviderSummary) key.attachment();
				clientInstance = PlugsFactory.getPluginsInstanceNew(ClientInstance.class);
				clientInstance.init(clientService, socketChannel,antProviderSummary);
				clientService.handler(socketChannel, clientInstance);
				logger.debug("Socket channel connected:" + clientInstance);
				AntRegisterHandler registerHandler = new AntRegisterHandler(clientInstance);
				clientService.getAntRegisterServcie().register(registerHandler);
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