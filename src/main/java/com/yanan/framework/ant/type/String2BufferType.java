package com.yanan.framework.ant.type;

import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.autowired.enviroment.Adapter;
import com.yanan.framework.plugin.autowired.enviroment.ResourceAdapter;
import com.yanan.utils.string.StringUtil;

@Adapter
@Register
public class String2BufferType implements ResourceAdapter<String, BufferType>{

	@Override
	public BufferType parse(String input) {
		return StringUtil.equals(input, "DIRECT")?BufferType.DIRECT:BufferType.HEAP;
	}

}
