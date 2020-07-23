package com.yanan.frame.ant.implement;

import com.yanan.frame.ant.interfaces.AntService;
import com.yanan.frame.plugin.annotations.Register;

@Register
public class AntServiceRegister implements AntService{
	@Override
	public String ping() {
		return "Plugin.Ant @yanan 2019-2020 All Rights Reserve!";
	}
}