package com.yanan.framework.ant.implement;

import com.yanan.framework.ant.interfaces.AntService;
import com.yanan.framework.plugin.annotations.Register;

@Register
public class AntServiceRegister implements AntService{
	@Override
	public String ping() {
		return "Plugin.Ant @yanan 2019-2020 All Rights Reserve!";
	}
}