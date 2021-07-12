package com.yanan.framework.a.dispatcher;

public class MessagePrototype<T>{
	/**
	 * 消息的请求ID
	 */
	private int RID;
	/**
	 * 消息类型 参考MessageType
	 */
	private int type;
	/**
	 * 调用信息
	 */
	private T invoker;
	/**
	 * 消息超时
	 */
	private int timeout;
	public int getRID() {
		return RID;
	}
	public void setRID(int rID) {
		RID = rID;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public T getInvoker() {
		return invoker;
	}
	public void setInvoker(T invoker) {
		this.invoker = invoker;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}