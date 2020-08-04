package com.yanan.framework.ant.model;

public class AntProviderSummary {
	/**
	 * Ant服务ID
	 */
	private String id;
	/**
	 * Ant服务名
	 */
	private String name;
	/**
	 * Ant服务地址
	 */
	private String host;
	/**
	 * Ant服务对外服务端口
	 */
	private int port;
	/**
	 * 附加信息
	 */
	private Object attach;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public Object getAttach() {
		return attach;
	}
	public void setAttach(Object attach) {
		this.attach = attach;
	}
	@Override
	public String toString() {
		return "AntProviderSummary [id=" + id + ", name=" + name + ", host=" + host + ", port=" + port + ", attach="
				+ attach + "]";
	}
}