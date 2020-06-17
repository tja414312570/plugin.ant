package com.YaNan.frame.ant.abstracts;


import com.YaNan.frame.ant.handler.AntServiceInstance;
import com.YaNan.frame.ant.interfaces.AntMessageSerialization;
import com.YaNan.frame.ant.model.AntMessagePrototype;
import com.YaNan.frame.ant.service.AntRuntimeService;

public interface ClientInstance {

	void write(AntMessagePrototype message);

	void close(Throwable cause) ;

	AntServiceInstance getServiceInstance();

	String getRemoteAddress();

	AntMessageSerialization getSerailzationHandler();
	
	AntRuntimeService getRuntimeService();

}
