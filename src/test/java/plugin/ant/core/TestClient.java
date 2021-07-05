package plugin.ant.core;

import com.yanan.framework.ant.AntContext;
import com.yanan.framework.ant.AntContextConfigure;
import com.yanan.framework.plugin.Environment;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.decoder.StandScanResource;
import com.yanan.utils.resource.ResourceManager;

import plugin.ant.a.RPCTest;

public class TestClient {
	public static void main(String[] args) {
		Environment.getEnviroment().registEventListener(PlugsFactory.getInstance().getEventSource(), event->{
//			System.err.println(event);
		});
		//环境初始化
		PlugsFactory.init(ResourceManager.getResource("classpath:plugin.yc"),
				new StandScanResource(ResourceManager.getClassPath(AntContext.class)[0]+"**"),
				new StandScanResource(ResourceManager.getClassPath(RPCTest.class)[0]+"**"));
		//获取配置
		AntContextConfigure antContextConfigure = new AntContextConfigure();
		//初始化上下文
		AntContext antContext = PlugsFactory.getPluginsInstance(AntContext.class,antContextConfigure);
		//开启服务
		antContext.start();
		
		
	}
}
