package com.yanan.frame.ant.handler;

import java.lang.reflect.Field;

import com.typesafe.config.Config;
import com.yanan.frame.ant.type.BufferType;
import com.yanan.utils.config.ConfigResolver;

public class BufferTypeDecoder implements ConfigResolver{

	@Override
	public Object decode(Field filed, Config config, Object instance) {
		String bufferTypeStr = config.getString("bufferType");
		if(bufferTypeStr.toLowerCase().equals("direct"))
			return BufferType.DIRECT;
		return BufferType.HEAP;
	}


}