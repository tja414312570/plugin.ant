package com.yanan.framework.a.dispatcher;

public class MessagePrototype<T>{
	/**
	 * 消息的请求ID
	 */
	protected int RID;
	/**
	 * 消息类型 参考MessageType
	 */
	protected int type;
	/**
	 * 调用信息
	 */
	protected T invoker;
	/**
	 * 消息超时
	 */
	protected int timeout;
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