package com.yanan.frame.ant.model;

public class AntCustomer {
	/**
	 * 注册名
	 */
	private String name;
	/**
	 * 注册秘钥
	 */
	private Object key;
	/**
	 * 注册附件
	 */
	private Object attach;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Object getKey() {
		return key;
	}
	public void setKey(Object key) {
		this.key = key;
	}
	public Object getAttach() {
		return attach;
	}
	public void setAttach(Object attach) {
		this.attach = attach;
	}
	@Override
	public String toString() {
		return "AntCustomer [name=" + name + ", key=" + key + ", attach=" + attach + "]";
	}
}