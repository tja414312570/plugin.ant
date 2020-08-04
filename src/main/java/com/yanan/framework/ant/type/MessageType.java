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
}