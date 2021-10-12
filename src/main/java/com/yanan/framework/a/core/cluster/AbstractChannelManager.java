package com.yanan.framework.a.core.cluster;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.server.ServerMessageChannel;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.utils.asserts.Assert;
import com.yanan.utils.reflect.TypeToken;

/**
 * 发现服务
 * @author YaNan
 *
 */
public abstract class AbstractChannelManager<K,N> implements ChannelManager<K>{

	@Service
	private Logger logger;
//	private List<ServerMessageChannel<?>> serverMessageChannelList = new CopyOnWriteArrayList<>();
	protected ServerMessageChannel<?> serverMessageChannel;
	protected Class<?> discoveryServer;
	@PostConstruct
	public void init() {
		discoveryServer =  PlugsFactory.getPluginsHandler(this).getRegisterDefinition().getRegisterClass();
		System.err.println("hello , this is test info , build by yanan !");
	}
	@Override
	public void start(ServerMessageChannel<?> channel) {
		//查看本地是否有MessageChannel
		this.start();
		this.startServerChannel(channel);
	}
	public void startServerChannel(ServerMessageChannel<?> channel) {
		logger.debug("开启服务提供通道:"+channel);
		this.serverMessageChannel = channel;
		channel.open();
		logger.debug("注册服务..."); 
		K serverName = getServerName(channel);
		this.registerChannel(serverName, channel);
	}
	
	public K getServerName(ServerMessageChannel<?> channel) {
		Class<?> channelClass = PlugsFactory.getPluginsHandler(channel).getRegisterDefinition().getRegisterClass();
		logger.debug("通道类:"+channelClass);
		logger.debug("通道管理类:"+discoveryServer);
		String nameSchme = channelClass.getSimpleName()+"_"+discoveryServer.getSimpleName();
		logger.debug("命名规则:"+nameSchme);
		ChannelNamingServer<K> namingServer = 
				PlugsFactory.getPluginsInstanceByAttributeStrict(new TypeToken<ChannelNamingServer<K>>() {}.getTypeClass(),
						nameSchme);
		Assert.isNotNull(namingServer,"没有找到命名规则服务");
		K serverName = namingServer.getServerName(channel);
		return serverName;
	}
	public <I> N getChannelName(I name) {
		Assert.isNotNull(name,"调用名称为空");
		Class<?> nameClass = name.getClass();
		logger.debug("名称类:"+nameClass);
		logger.debug("通道管理类:"+discoveryServer);
		String nameSchme = nameClass.getSimpleName()+"_"+discoveryServer.getSimpleName();
		logger.debug("命名规则:"+nameSchme);
		ChannelInstanceNameServer<N,I> namingServer = 
				PlugsFactory.getPluginsInstanceByAttributeStrict(
						new TypeToken<ChannelInstanceNameServer<N,I>>() {}.getTypeClass(),
						nameSchme);
		Assert.isNotNull(namingServer,"没有找到命名规则服务");
		N serverName = namingServer.getInstanceName(name);
		return serverName;
	}
	@SuppressWarnings("unchecked")
	public <T> ServerMessageChannel<T> getServerChannel(){
		return (ServerMessageChannel<T>) serverMessageChannel;
	}
	public <T> MessageChannel<T> getChannel(K name){
		N serverName = getChannelName(name);
		return getChannelInstance(serverName);
	}
	public <T> List<MessageChannel<T>> getChannelList(K name){
		N serverName = getChannelName(name);
		return getChannelInstanceList(serverName);
	}
	protected abstract <T> List<MessageChannel<T>> getChannelInstanceList(N serverName);
	public abstract <T> MessageChannel<T> getChannelInstance(N name);
}
