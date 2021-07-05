package com.yanan.framework.a.core.cluster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.yanan.framework.a.channel.socket.server.ChannelNamingServer;
import com.yanan.framework.a.core.server.ServerMessageChannel;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.utils.reflect.TypeToken;

/**
 * 发现服务
 * @author YaNan
 *
 */
public abstract class AbstractChannelManager<K> implements ChannelManager<K>{

	@Service
	private Logger logger;
	private List<ServerMessageChannel<?>> serverMessageChannelList = new CopyOnWriteArrayList<>();
	@PostConstruct
	public void init() {
		System.err.println("hello world");
	}
	@Override
	public void start(ServerMessageChannel<?> channel) {
		//查看本地是否有MessageChannel
		this.start();
		this.startServerChannel(channel);
	}
	public void startServerChannel(ServerMessageChannel<?> channel) {
		logger.debug("开启服务提供通道:"+channel);
		this.serverMessageChannelList.add(channel);
		channel.open();
		logger.debug("注册服务..."); 
		K serverName = getServerName(channel);
		this.registerChannel(serverName, channel);
	}
	
	public K getServerName(ServerMessageChannel<?> channel) {
		Class<?> channelClass = PlugsFactory.getPluginsHandler(channel).getRegisterDefinition().getRegisterClass();
		logger.debug("通道类:"+channelClass);
		Class<?> discoveryServer =  PlugsFactory.getPluginsHandler(this).getRegisterDefinition().getRegisterClass();
		logger.debug("通道管理类:"+discoveryServer);
		String nameSchme = channelClass.getSimpleName()+"_"+discoveryServer.getSimpleName();
		logger.debug("命名规则:"+nameSchme);
		System.out.println("");
		ChannelNamingServer<K> namingServer = 
				PlugsFactory.getPluginsInstanceByAttributeStrict(new TypeToken<ChannelNamingServer<K>>() {}.getTypeClass(),
						nameSchme);
		if(namingServer == null)
			throw new RuntimeException("没有找到命名规则服务");
		K serverName = namingServer.getServerName(channel);
		return serverName;
	}
	public List<ServerMessageChannel<?>> getSserverMessageChannelList() {
		return serverMessageChannelList;
	}

}
