package com.YaNan.frame.ant.test;

import com.YaNan.frame.ant.AntContextConfigure;
import com.YaNan.frame.utils.config.ConfigHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigHelperTest {
	public static void main(String[] args) {
		Config config = ConfigFactory.load("Ant.conf");
		System.out.println(config);
		Config ant = config.getConfig("Ant");
		System.out.println(ant);
		AntContextConfigure conf = ConfigHelper.decode(ant, AntContextConfigure.class);
		System.out.println(conf);
	}
}
