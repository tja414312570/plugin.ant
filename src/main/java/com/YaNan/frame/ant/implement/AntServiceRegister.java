package com.YaNan.frame.ant.implement;

import com.YaNan.frame.ant.interfaces.AntService;
import com.YaNan.frame.plugin.annotations.Register;

@Register
public class AntServiceRegister implements AntService{
	@Override
	public String ping() {
		return "Plugin.Ant @YaNan 2019-2020 All Rights Reserve!";
	}
}
