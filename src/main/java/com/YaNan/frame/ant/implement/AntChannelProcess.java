package com.YaNan.frame.ant.implement;

import java.nio.channels.SelectionKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.abstracts.AbstractProcess;
import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.handler.AntRegisterHandler;
import com.YaNan.frame.ant.model.AntProviderSummary;

public class AntChannelProcess extends AbstractProcess {
	private AntClientHandler handler;
	private SelectionKey key;
	private int ops;
	private static Logger logger = LoggerFactory.getLogger(AntChannelProcess.class);

	public AntChannelProcess(AntClientHandler handler, int ops, SelectionKey key) {
		this.handler = handler;
		this.ops = ops;
		this.key = key;
	}

	@Override
	public void execute() {
		if (ops == SelectionKey.OP_READ) {
			handler.handleRead(key);
		} else if (ops == SelectionKey.OP_WRITE) {

		} else if (ops == SelectionKey.OP_CONNECT) {
			AntProviderSummary antProviderSummary = (AntProviderSummary) key.attachment();
			handler.setAttribute(AntProviderSummary.class, antProviderSummary);
			logger.debug("Socket channel connected:" + handler);
			AntRegisterHandler registerHandler = new AntRegisterHandler(handler);
			handler.getRuntimeService().getAntRegisterServcie().register(registerHandler);
		} else if (ops == SelectionKey.OP_ACCEPT) {
			handler.getRuntimeService().getAntClientRegisterService().register(handler);
		}

	}

}
