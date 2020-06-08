package com.YaNan.frame.ant.model;

/**
 * Ant 提供者注册
 * @author yanan
 */
public class AntProvider {
	/**
	 * 服务名
	 */
	private String name;
	/**
	 * 服务端口
	 */
	private int port;
	/**
	 * 注册秘钥
	 */
	private Object key;
	/**
	 * 服务地址
	 */
	private String host;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public Object getKey() {
		return key;
	}
	public void setKey(Object key) {
		this.key = key;
	}
	@Override
	public String toString() {
		return "AntProvider [name=" + name + ", port=" + port + ", key=" + key + "]";
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
}
