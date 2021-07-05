package plugin.ant.a;

import com.yanan.framework.a.channel.socket.message.AbstractMessage;
import com.yanan.framework.a.channel.socket.message.Message;
import com.yanan.framework.a.channel.socket.server.MessageChannelCreateListener;
import com.yanan.framework.a.core.MessageChannel;
import com.yanan.framework.a.core.MessageHandler;
import com.yanan.framework.a.core.server.ServerMessageChannel;
import com.yanan.framework.ant.AntContext;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.decoder.StandScanResource;
import com.yanan.utils.resource.ResourceManager;

public class MessageChannelServerTest {
	public static void main(String[] args) throws InterruptedException {
		//环境初始化
		PlugsFactory.init(ResourceManager.getResource("classpath:plugin.yc"),
				new StandScanResource(ResourceManager.getClassPath(AntContext.class)[0]+"**"),
				new StandScanResource(ResourceManager.getClassPath(MessageChannelServerTest.class)[0]+"**"));
		//序列化工具
//		MessageSerially messageSerially =  PlugsFactory.getPluginsInstance(MessageSerially.class);
		//启用服务调用者
		ServerMessageChannel<String> client =  PlugsFactory.getPluginsInstance(ServerMessageChannel.class);
		client.onChannelCreate(new MessageChannelCreateListener<String>() {

			@Override
			public void onCreate(MessageChannel<String> messageChannel) {
				messageChannel.accept(new MessageHandler<String>() {
					@Override
					public void onMessage(String message) {
						System.out.println("读取消息:"+message+",来自:"+messageChannel);
					}
				});
				
				System.err.println("一个新的服务连接:"+messageChannel);
				AbstractMessage message = new AbstractMessage();
				message.setMessage("hello weclome!bye\r\n");
				int i = 0;
				while(i++<500) {
//					messageChannel.transport(message);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//				messageChannel.close();
			}
		});
		//建立链接
		client.open();
//		//通信
//		client.close();
//		provider.close();
		synchronized (client) {
			client.wait();
		}
		
	}
}
