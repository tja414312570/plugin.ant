package com.YaNan.frame.ant.handler;

import java.lang.reflect.Field;

import com.YaNan.frame.ant.type.BufferType;
import com.YaNan.frame.utils.config.ConfigResolver;
import com.typesafe.config.Config;

public class BufferTypeDecoder implements ConfigResolver{

	@Override
	public Object decode(Field filed, Config config, Object instance) {
		String bufferTypeStr = config.getString("bufferType");
		if(bufferTypeStr.toLowerCase().equals("direct"))
			return BufferType.DIRECT;
		return BufferType.HEAP;
	}


}
