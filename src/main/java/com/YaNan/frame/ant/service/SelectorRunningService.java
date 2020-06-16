package com.YaNan.frame.ant.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.implement.AntChannelProcess;
import com.YaNan.frame.ant.implement.ProcessProvider;
import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.ant.utils.ObjectLock;
import com.YaNan.frame.ant.utils.SelectedSelectionKeySet;

/**
 * Selector运行时服务
 * 
 * @author yanan
 *
 */
public class SelectorRunningService implements Runnable {
	private AntRuntimeService runtimeService;
	private static Logger logger = LoggerFactory.getLogger(SelectorRunningService.class);
	private Selector selector;
	private boolean DISABLE_KEYSET_OPTIMIZATION;
	private SelectedSelectionKeySet selectedKeySet;

	public Selector getSelector() {
		return selector;
	}

	private void openSelector() {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (DISABLE_KEYSET_OPTIMIZATION) {
			return;
		}
		try {
			selectedKeySet = new SelectedSelectionKeySet();
			Class<?> selectorImplClass = Class.forName("sun.nio.ch.SelectorImpl", false, getClass().getClassLoader());
			// Ensure the current selector implementation is what we can instrument.
			if (!selectorImplClass.isAssignableFrom(selector.getClass())) {
				DISABLE_KEYSET_OPTIMIZATION = true;
				return;
			}
			Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
			Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");

			selectedKeysField.setAccessible(true);
			publicSelectedKeysField.setAccessible(true);

			selectedKeysField.set(selector, selectedKeySet);
			publicSelectedKeysField.set(selector, selectedKeySet);
			logger.debug("Instrumented an optimized java.util.Set into: {}", selector);
		} catch (Throwable t) {
			logger.debug("Failed to instrument an optimized java.util.Set into: {}", selector, t);
		}
	}

	public SelectorRunningService(AntRuntimeService antRuntimeService) {
		runtimeService = antRuntimeService;
		// 创建选择器
		openSelector();
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (selector.select(1000) == 0) {// 强制50ms执行一次，因为需要先select后register，造成死锁
					continue;
				}
				if (!DISABLE_KEYSET_OPTIMIZATION) {
					int len = selectedKeySet.size();
					SelectionKey[] keys = selectedKeySet.flip();
					for (int i = 0; i < len; i++) {
						SelectionKey key = keys[i];
						processKey(key);
					}
				} else {
					Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
					while (keyIter.hasNext()) {
						SelectionKey key = keyIter.next();
						keyIter.remove();
						processKey(key);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void processKey(SelectionKey key) throws IOException {
		SocketChannel socketChannel;
		AntClientHandler handler;
		try {
			// 客户端连接到此设备
			if (key.isAcceptable()) {
				logger.debug("accept a service connection!");
				socketChannel = ((ServerSocketChannel) key.channel()).accept();
				logger.debug("service connection ip:" + socketChannel.getRemoteAddress());
				socketChannel.configureBlocking(false);
				socketChannel.register(key.selector(), SelectionKey.OP_READ);
				handler = new AntClientHandler(socketChannel, runtimeService);
				runtimeService.executeProcess(new AntChannelProcess(handler, SelectionKey.OP_ACCEPT, key));
				runtimeService.getAntClientRegisterService().register(handler);
				runtimeService.handlerMapping(socketChannel, handler);
				return;
			}
			socketChannel = (SocketChannel) key.channel();
			// 连接到目标
			if (key.isConnectable()) {
				logger.debug("connect to service success:" + socketChannel.getRemoteAddress());
				handler = new AntClientHandler(socketChannel, runtimeService);
				key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				runtimeService.handlerMapping(socketChannel, handler);
				runtimeService.executeProcess(new AntChannelProcess(handler, SelectionKey.OP_CONNECT, key));
			}
			handler = runtimeService.getHandler(socketChannel);
			if (handler == null) {
				socketChannel.close();
			}
			// 可读
			if (key.isReadable()) {
				runtimeService
						.executeProcess(ProcessProvider.requireChannerlProcess(handler, SelectionKey.OP_READ, key));
			}
			// 可写
			if (key.isWritable()) {
//				                	handler.handlWrite(key);
			}
		} catch (Throwable e) {
			AntProviderSummary summary = (AntProviderSummary) key.attachment();
			if (summary != null) {
				ObjectLock.getLock(summary.getName()).release();
			}
		}
	}

}