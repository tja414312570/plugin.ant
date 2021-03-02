package com.yanan.framework.ant.model;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.yanan.framework.ant.handler.AntServiceInstance;
import com.yanan.framework.ant.type.MessageType;
import com.yanan.framework.ant.utils.InvokeInfo;
import com.yanan.framework.ant.utils.InvokeInfoCache;
import com.yanan.framework.plugin.ExtReflectUtils;
import com.yanan.utils.ByteUtils;

public class AntMessagePrototype{
	/**
	 * 服务
	 */
	protected String service;
	/**
	 * 消息的请求ID
	 */
	protected int RID;
	/**
	 * 消息类型 参考MessageType
	 */
	protected int type;
	/**
	 * 消息调用的类
	 */
	protected Class<?> invokeClass;
	/**
	 * 消息调用的方法
	 */
	protected Method invokeMethod;
	/**
	 * 消息参数
	 */
	protected Object[] invokeParmeters;
	/**
	 * 内容缓存
	 * @param requestClass
	 */
	protected byte[] buffered;
	/**
	 * 信息头长度
	 */
	protected int invokeHeaderLen;
	/**
	 * 消息超时
	 */
	private int timeout;
	/**
	 * 消息是否已经解析
	 */
	private boolean decode = false;
	private AntServiceInstance clientHandler;
	public int getInvokeHeaderLen() {
		return invokeHeaderLen;
	}
	public void setInvokeHeaderLen(int invokeHeaderLen) {
		this.invokeHeaderLen = invokeHeaderLen;
	}
	public Class<?> getInvokeClass() {
		return invokeClass;
	}
	public void setInvokeClass(Class<?> invokeClass) {
		this.invokeClass = invokeClass;
	}
	public Method getInvokeMethod() {
		return invokeMethod;
	}
	public void setInvokeMethod(Method invokeMethod) {
		this.invokeMethod = invokeMethod;
	}
	public Object[] getInvokeParmeters() {
		return invokeParmeters;
	}
	public void setInvokeParmeters(Object... invokeParmeters) {
		this.invokeParmeters = invokeParmeters;
	}
	public byte[] getBuffered() {
		return buffered;
	}
	public void setBuffered(byte[] buffered) {
		this.buffered = buffered;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getRID() {
		return RID;
	}
	public void setRID(int rID) {
		RID = rID;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	//解析请求
	public void decode() throws ClassNotFoundException, NoSuchMethodException, SecurityException{
		//判断是否已经解析
		if(!decode){
			//判断缓存是否有数据
			if(this.buffered!=null&&this.buffered.length!=0){
				//获取消息头信息以获取调用的类和方法
				if(this.invokeHeaderLen>0){
					String invokeInfoStr = new String(this.buffered,0,this.invokeHeaderLen);
					//这里加入一个缓存 希望能提高寻找方法的速度
					InvokeInfo invokeInfo = InvokeInfoCache.getInvokeInfo(invokeInfoStr);
					if(invokeInfo == null){
						String[] infoFragment = invokeInfoStr.split(":");
						Class<?> iClass = Class.forName(infoFragment[0]);
						Method iMethod = null ;
						if(infoFragment.length>1) {
							Class<?>[] parameterType = new Class<?>[infoFragment.length-2];
							for(int i = 0;i<infoFragment.length-2;i++)
								parameterType[i] = ExtReflectUtils.getParameterType(infoFragment[i+2]);
							iMethod = iClass.getMethod(infoFragment[1], parameterType);
						}
						invokeInfo = new InvokeInfo(iClass, iMethod);
						InvokeInfoCache.addInvokeInfo(invokeInfoStr, invokeInfo);
					}
					this.invokeClass = invokeInfo.getInvokeClass();
					this.invokeMethod = invokeInfo.getInvokeMethod();
				}
				//读取调用参数的数据
				if(this.buffered.length == this.invokeHeaderLen) {
					this.invokeParmeters = null;
				}else {
					//通过调用头判断是返回值还是方法的参数
					Class<?>[] type = null;
					ByteBuffer buffer = ByteBuffer.wrap(this.buffered);
					buffer.position(this.invokeHeaderLen);
					//获取参数个数
					int nums = buffer.get() & 0xff;
					//生成数组
					this.invokeParmeters = new Object[nums];
					byte[] paramLenByte = new byte[4];
					int paramLen = 0;
					int position = buffer.position();
					int limit = 0;
					int recoderLimit = buffer.limit();
					//获取序列化类型的参数
					if(this.type == MessageType.RESPONSE) {
						type = new Class<?>[1];
						type[0] = this.invokeMethod.getReturnType();
					}else {
						type = this.invokeMethod.getParameterTypes();
					}
					//反序列化
					for(int i = 0;i<nums;i++) {
						buffer.limit(recoderLimit);
						buffer.get(paramLenByte);
						paramLen = ByteUtils.bytesToInt(paramLenByte);
						position+=4;
						limit = position+paramLen;
						if(position != limit) {
							Class<?> t = i<type.length?type[i]:type[type.length-1];
							this.invokeParmeters[i]  = AntServiceInstance.getServiceInstance().getClientInstance().getSerailzationHandler()
									.deserializationAntMessage(buffer,position, limit,t);
						}
						position = limit;
					}
				}
			}
			this.decode = true;
		}
	}
	//解析注册信息
	public void decode(Class<?> decodeType){
		if(this.buffered.length>0){
			this.invokeParmeters = new String[1];
			ByteBuffer buffer = ByteBuffer.wrap(this.buffered);
			this.invokeParmeters[0] = AntServiceInstance.getServiceInstance().getClientInstance().getSerailzationHandler()
			.deserializationAntMessage(buffer,5, this.buffered.length,decodeType);
		}
	}
	@Override
	public String toString() {
		return "AntMessagePrototype [service=" + service + ", RID=" + RID + ", type=" + type + ", invokeClass="
				+ invokeClass + ", invokeMethod=" + invokeMethod + ", invokeParmeters=" + Arrays.toString(invokeParmeters)
				+  ", invokeHeaderLen=" + invokeHeaderLen + ", decode="
				+ decode + ", buffered=" + Arrays.toString(buffered) +"]";
	}
	public void decodeException() {
		this.decode(String.class);
	}
	public AntServiceInstance getClientHandler() {
		return clientHandler;
	}
	public void setClientHandler(AntServiceInstance clientHandler) {
		this.clientHandler = clientHandler;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}