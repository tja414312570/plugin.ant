package com.YaNan.frame.ant.model;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;


import com.YaNan.frame.ant.handler.AntClientHandler;

public class AntProviderContext {
	private int id;
	private String name;
	private AntProvider provider;
	private AntClientHandler handler;
	private AntProviderSummary providerSummary ;
	public AntProviderContext(AntProvider provider, AntClientHandler handler) {
		if(handler != null)
			this.id = handler.getId();
		this.provider = provider;
		this.handler = handler;
		this.name = provider.getName();
		providerSummary = new AntProviderSummary();
		providerSummary.setId(this.id);
		
		try {
			String host = null;
			if((provider.getHost() == null || provider.getHost().equals("")) && handler != null) {
				SocketAddress address;
				address = handler.getSocketChannel().getRemoteAddress();
				if(address.getClass().equals(InetSocketAddress.class)) {
					host = ((InetSocketAddress)address).getAddress().getHostAddress();
				}
//				else {
//				host = ((SshdSocketAddress)address).getHostName();
//				}
			}else {
				host = provider.getHost();
			}
			providerSummary.setHost(host);
		} catch (IOException e) {
			e.printStackTrace();
		}
		providerSummary.setPort(provider.getPort());
		providerSummary.setName(this.name);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public AntClientHandler getHandler() {
		return handler;
	}
	public void setHandler(AntClientHandler handler) {
		this.handler = handler;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public AntProvider getProvider() {
		return provider;
	}
	public void setProvider(AntProvider provider) {
		this.provider = provider;
	}
	public AntProviderSummary getProviderSummary() {
		return providerSummary;
	}
	public void setProviderSummary(AntProviderSummary providerSummary) {
		this.providerSummary = providerSummary;
	}
	@Override
	public String toString() {
		return "AntProviderContext [id=" + id + ", name=" + name + ", provider=" + provider + ", handler=" + handler
				+ ", providerSummary=" + providerSummary + "]";
	}
}
