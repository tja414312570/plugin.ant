package plugin.ant.a;

import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.cluster.ChannelManager;
import com.yanan.framework.a.core.server.ServerMessageChannel;
import com.yanan.framework.ant.AntContext;
import com.yanan.framework.plugin.Environment;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.decoder.StandScanResource;
import com.yanan.utils.resource.ResourceManager;

public class DiscoveryServiceServerTest {
	public static void main(String[] args) throws InterruptedException {
		Environment.getEnviroment().registEventListener(PlugsFactory.getInstance().getEventSource(), event->{
//			System.err.println(event);
		});
		//环境初始化
		PlugsFactory.init(ResourceManager.getResource("classpath:plugin.yc"),
				new StandScanResource(ResourceManager.getClassPath(AntContext.class)[0]+"**"),
				new StandScanResource(ResourceManager.getClassPath(DiscoveryServiceServerTest.class)[0]+"**"));
		//启用服务调用者
		ChannelManager<String> server =  PlugsFactory.getPluginsInstance(ChannelManager.class);
		ServerMessageChannel<String> client =  PlugsFactory.getPluginsInstance(ServerMessageChannel.class);
		server.start(client);
		//获取其他注册中心
		MessageChannel<String> messageChannel = server.getChannel(null);
	}
}
