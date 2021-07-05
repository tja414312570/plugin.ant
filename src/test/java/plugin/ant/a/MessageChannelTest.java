package plugin.ant.a;

import com.yanan.framework.a.channel.socket.message.AbstractMessage;
import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.MessageHandler;
import com.yanan.framework.ant.AntContext;
import com.yanan.framework.plugin.Environment;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.decoder.StandScanResource;
import com.yanan.utils.resource.ResourceManager;

public class MessageChannelTest {
	public static void main(String[] args) throws InterruptedException {
		Environment.getEnviroment().registEventListener(PlugsFactory.getInstance().getEventSource(), event->{
//			System.err.println(event);
		});
//		 PlugsFactory.getPluginsInstance(SocketMessageChannel.class);
		//环境初始化
		PlugsFactory.init(ResourceManager.getResource("classpath:plugin.yc"),
				new StandScanResource(ResourceManager.getClassPath(AntContext.class)[0]+"**"),
				new StandScanResource(ResourceManager.getClassPath(MessageChannelTest.class)[0]+"**"));
		//序列化工具
//		MessageSerially messageSerially =  PlugsFactory.getPluginsInstance(MessageSerially.class);
		//启用服务调用者
		MessageChannel<String> client =  PlugsFactory.getPluginsInstance(MessageChannel.class);
		//建立链接
		client.open();
		client.accept(new MessageHandler<String>() {
			
			@Override
			public void onMessage(String message) {
				System.out.println("读取消息:"+message+",来自:"+client);
			}
		});
		AbstractMessage message = new AbstractMessage();
		message.setMessage("hello world");
//		//通信
		client.transport("hello world");
//		//关闭
//		client.close();
//		ExecutorServer execurotServer = PlugsFactory.getPluginsInstance(ExecutorServer.class);
//		execurotServer.shutdown();
//		System.out.println(execurotServer.isShutdown());
//		provider.close();
//		synchronized (client) {
//			client.wait();
//		}
		
	}
}
