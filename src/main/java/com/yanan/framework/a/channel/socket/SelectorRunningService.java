package com.yanan.framework.a.channel.socket;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.framework.a.process.ExecutorServer;
import com.yanan.framework.ant.utils.SelectedSelectionKeySet;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;

/**
 * Selector运行时服务
 * 
 * @author yanan
 *
 */
@Register
public class SelectorRunningService implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(SelectorRunningService.class);
	private Selector selector;
	private boolean DISABLE_KEYSET_OPTIMIZATION;
	@Service
	private ExecutorServer executorServer;
	private Set<AbstractSelectableChannel> channelList = Collections
			.synchronizedSet(new HashSet<AbstractSelectableChannel>());
	private SelectedSelectionKeySet selectedKeySet;
	private volatile boolean available;

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

	public SelectorRunningService() {
		// 创建选择器
		openSelector();
	}

	public void registerChannel(AbstractSelectableChannel messageChannel) {
		this.channelList.add(messageChannel);
		this.loopSelector();
	}

	public void removeChannel(AbstractSelectableChannel messageChannel) {
		this.channelList.remove(messageChannel);
		if (this.channelList.isEmpty())
			this.close();
	}

	public synchronized void loopSelector() {
		if (!this.available) {
			this.available = true;
			executorServer.execute(this);
		}
	}

	public synchronized void close() {
		this.available = false;
	}

	@Override
	public void run() {
		while (available) {
//			System.err.println("轮训");
			try {
				if (selector.selectNow() == 0) {
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
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
	}

	public void processKey(SelectionKey key) throws IOException {
		SocketChannel socketChannel;
//		if(!key.isValid() || !key.isConnectable())
//			return;
		try {
			// 客户端连接到此设备
			if (key.isAcceptable()) {
				logger.debug("accept a service connection!");
				socketChannel = ((ServerSocketChannel) key.channel()).accept();
				logger.debug("service connection ip:" + socketChannel.getRemoteAddress());
				executorServer.execute(PlugsFactory.getPluginsInstance(SocketChannelProcess.class, socketChannel,
						SelectionKey.OP_ACCEPT, key));
//				PlugsFactory.getPluginsInstance(SocketChannelProcess.class,socketChannel,  SelectionKey.OP_CONNECT, key).run();
				return;
			}
			socketChannel = (SocketChannel) key.channel();
			// 连接到目标
			if (key.isConnectable()) {
				logger.debug("connect to service:" + socketChannel.getRemoteAddress());
				key.interestOps(SelectionKey.OP_READ);
				executorServer.executeProcess(PlugsFactory.getPluginsInstance(SocketChannelProcess.class, socketChannel,
						SelectionKey.OP_CONNECT, key));
//				PlugsFactory.getPluginsInstance(SocketChannelProcess.class,socketChannel,  SelectionKey.OP_CONNECT, key).run();
			}
			// 可读
			if (key.isReadable()) {
				logger.debug("read to service:" + socketChannel.getRemoteAddress());
				executorServer.executeProcess(
						ProcessProvider.requireChannerlProcess(socketChannel, SelectionKey.OP_READ, key));
			}
			// 可写
//			if (key.isWritable()) {
////				                	handler.handlWrite(key);
//			}
		} catch (Throwable e) {
//			key.cancel();
			e.printStackTrace();
//			AntProviderSummary summary = (AntProviderSummary) key.attachment();
//			if (summary != null) {
//				ObjectLock.getLock(summary.getName()).release();
//			}
		}
	}

}