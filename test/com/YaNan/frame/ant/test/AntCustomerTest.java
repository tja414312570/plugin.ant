package com.YaNan.frame.ant.test;

import java.io.File;

import com.YaNan.frame.ant.AntContext;
import com.YaNan.frame.ant.interfaces.provider.AntProviderService;
import com.YaNan.frame.ant.model.AntCustomer;
import com.YaNan.frame.ant.model.RegisterResult;
import com.YaNan.frame.ant.service.AntRuntimeService;
import com.YaNan.frame.plugin.PlugsFactory;

public class AntCustomerTest {
	public static void main(String[] args) {
		PlugsFactory.getInstance().addScanPath("/Users/yanan/eclipse-workspace/plugin.ant.provider/target/classes");
		PlugsFactory.getInstance().addScanPath("/Users/yanan/eclipse-workspace/plugin.ant.queen/target/classes");
		
		AntContext ant = AntContext.getContext();
		PlugsFactory.getInstance().init0();
		File file = new File("/Users/yanan/eclipse-workspace/plugin.ant.provider/test/Ant.conf");
		System.out.println(file.getAbsolutePath());
		ant.init(file);
		ant.start();
		System.out.println("服务开启完成======================");
		AntProviderService service = PlugsFactory.getPlugsInstance(AntProviderService.class);
		AntCustomer customer = new AntCustomer();
		customer.setName(AntContext.getContext().getContextConfigure().getHost());
		RegisterResult result = null;
		int all =1;
		long t = System.currentTimeMillis();
		for(int i= 0;i<all;i++) {
			result = service.registCustomer(customer);
			AntRuntimeService.getAntRuntimeService().getQueenHandler().releaseWriteLock();
		}
		float n = (System.currentTimeMillis() - t ) / 1000f;
		System.out.println("延时:"+n/all +"\t耗时："+n+"\t平均:"+(all/n));
		System.out.println(result);
	}
}
 