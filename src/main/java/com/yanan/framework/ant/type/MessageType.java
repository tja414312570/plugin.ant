package com.yanan.framework.ant.type;

/**
 * Ant消息类型
 * @author yanan
 *
 */
public interface MessageType {
	/**
	 * 请求类型,请求响应同步
	 */
	public final static int REQUEST= 0 ;
	/**
	 * 响应类型
	 */
	public final static int RESPONSE= 1 ;
	/**
	 * 响应异常
	 */
	public final static int EXCEPTION = 2 ;
	/**
	 * 注册类型
	 */
	@Deprecated
	public static final int REGISTER = -1;
	/**
	 * 通知类型
	 */
	public final static int NOTIFY = 3 ;
	
	/**
	 * 异步请求
	 */
	public final static int ASYNC_REQUEST = 4;
	
	/**
	 * 异步响应
	 */
	public final static int ASYNC_RESPONSE = 5;
	
	/**
	 * 异步响应结束
	 */
	public final static int ASYNC_END = 6;
}