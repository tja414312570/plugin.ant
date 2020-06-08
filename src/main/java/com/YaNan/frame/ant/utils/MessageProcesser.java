package com.YaNan.frame.ant.utils;

import java.lang.reflect.Method;

import com.YaNan.frame.ant.abstracts.AbstractProcess;
import com.YaNan.frame.ant.exception.AntInvoketionException;
import com.YaNan.frame.ant.exception.AntRequestException;
import com.YaNan.frame.ant.exception.AntResponseException;
import com.YaNan.frame.ant.handler.AntClientHandler;
import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.model.AntResponse;
import com.YaNan.frame.ant.proxy.AntInvokeProxy;
import com.YaNan.frame.ant.type.MessageType;
import com.YaNan.frame.plugin.PlugsFactory;

/**
 * 消息处理进程
 * @author yanan
 *
 */
public class MessageProcesser extends AbstractProcess{
	/**
	 * 处理的消息
	 */
	private AntMessagePrototype message;
	/**
	 * 处理所对应连接处理器
	 */
	private AntClientHandler clientHandler;
	public MessageProcesser(AntMessagePrototype message, AntClientHandler clientHandler) {
		this.message = message;
		this.clientHandler = clientHandler;
	}
	@Override
	public void execute() {
		int type = message.getType();
		switch (type) {
		case MessageType.REQUEST:
			doRequest();
			break;
		case MessageType.RESPONSE:
			doResponse();
			break;
		case MessageType.EXCEPTION:
			doException();
			break;
		}
	}

//	public void throwException(RPCException rpcException) throws ServiceClosed, ServiceNotFound, ServiceNotResponse, UnKnowException, InvoketionException{
//		switch(rpcException.getCode()){
//		case RPCExceptionType.SERVICE_CLOSED:
//			ServiceClosed exception=(ServiceClosed) rpcException;
//			throw new ServiceClosed(exception.getServiceName(),exception.getSUID(),exception.getRUID());
//		case RPCExceptionType.SERVICE_NO_FOUND:
//			ServiceNotFound snfException=(ServiceNotFound) rpcException;
//			throw new ServiceNotFound(snfException.getServiceName(),snfException.getRUID());
//		case RPCExceptionType.SERVICE_NOT_RESPONSE:
//			ServiceNotResponse snrException=(ServiceNotResponse) rpcException;
//			throw new ServiceNotResponse(snrException.getServiceName(),snrException.getSUID(),snrException.getRUID());
//		case RPCExceptionType.INVOKETION_EXCEPTION:
//			InvoketionException ivException=(InvoketionException) rpcException;
//			throw new InvoketionException(ivException.getServiceName(),ivException.getException(),ivException.getSUID(),ivException.getRUID());
//		default:
//			UnKnowException ue = (UnKnowException) rpcException;
//			throw new UnKnowException(ue.getServiceName(),rpcException.getSUID(),rpcException.getRUID());
//		}
//	}
	
	private void doException() {
		int ruid = message.getRID();
		message.decodeException();
		clientHandler.getRuntimeService().getMessageQueue().notifyException(ruid, new AntResponseException(message.getInvokeParmeters()[0]));
	}

	private void doResponse() {
		int ruid = message.getRID();
		clientHandler.getRuntimeService().getMessageQueue().notifyRequest(ruid,message);
	}

	private void doRequest() {
		// 获取调用信息
		try {
			//解析消息
			message.decode();
			//判断接口和方法
			if(message.getInvokeClass() == null || message.getInvokeMethod() == null)
				throw new AntRequestException("ant request interface or method is null");
			//获取接口实现
			Object instance = PlugsFactory.getPlugsInstance(message.getInvokeClass());
			//如果实现是否是Ant代理实现，抛出异常
			if(PlugsFactory.getPlugsHandler(instance).getRegisterDescription().getRegisterClass().equals(AntInvokeProxy.class))
				throw new AntRequestException("could not found register for '"+message.getInvokeClass()+"'");
			//获取调用方法
			Method method = message.getInvokeMethod();
			//调用方法
			Object rObj = method.invoke(instance,(Object[]) message.getInvokeParmeters());
			//判断是否需要响应
//			if(!method.getReturnType().equals(void.class)) {
				// 组装响应内容
				AntResponse response = new AntResponse();
				response.setRID(message.getRID());
				//将调用结果返回出去
				response.setInvokeParmeters(rObj);
				//写入消息
				clientHandler.write(response);
//			}
		} catch (Throwable e) {
			e.printStackTrace();
			clientHandler.write(new AntInvoketionException(
					e.toString() + (e.getCause() == null ? "Unknow Exception!" : "\nCase by:" + e.getCause()), message.getRID()));
		}
	}
}
