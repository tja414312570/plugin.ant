package plugin.ant.a;

import com.yanan.framework.a.channel.socket.message.AbstractMessage;
import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.MessageHandler;
import com.yanan.framework.a.core.cluster.ChannelManager;
import com.yanan.framework.ant.AntContext;
import com.yanan.framework.plugin.Environment;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.decoder.StandScanResource;
import com.yanan.utils.resource.ResourceManager;

public class DiscoveryServiceTest {
	public static void main(String[] args) throws InterruptedException {
		Environment.getEnviroment().registEventListener(PlugsFactory.getInstance().getEventSource(), event->{
//			System.err.println(event);
		});
//		 PlugsFactory.getPluginsInstance(SocketMessageChannel.class);
		//环境初始化
		PlugsFactory.init(ResourceManager.getResource("classpath:plugin.yc"),
				new StandScanResource(ResourceManager.getClassPath(AntContext.class)[0]+"**"),
				new StandScanResource(ResourceManager.getClassPath(DiscoveryServiceTest.class)[0]+"**"));
		//序列化工具
//		MessageSerially messageSerially =  PlugsFactory.getPluginsInstance(MessageSerially.class);
		//启用服务调用者
		ChannelManager<String> client =  PlugsFactory.getPluginsInstance(ChannelManager.class);
		//建立链接
		client.start();
		MessageChannel<String> messageChannel = client.getChannel("xxxxxxx");
		System.out.println(messageChannel);
	}
}
