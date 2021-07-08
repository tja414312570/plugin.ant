package plugin.ant.a;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import com.yanan.framework.a.channel.socket.message.AbstractMessage;
import com.yanan.framework.a.core.BufferReady;
import com.yanan.framework.a.core.ByteBufferChannel;
import com.yanan.framework.ant.AntContext;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.decoder.StandScanResource;
import com.yanan.utils.reflect.TypeToken;
import com.yanan.utils.resource.ResourceManager;

@Register()
public abstract class ByteBufferedMessageChannel<T> {
	private Type type;

	ByteBufferedMessageChannel(){
		type = new TypeToken<T>() {}.getType();
		System.out.println(type);
	}
	public static void main(String[] args) {
//		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D:\\cglib");  //--该设置用于输出cglib动态代理产生的类
//		System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true"); 
//		Object obj = PlugsFactory.getPluginsInstance(new TypeToken<ByteBufferedMessageChannel<String>>() {}.getTypeClass());
//		System.out.println(obj.getClass());
		PlugsFactory.init(ResourceManager.getResource("classpath:plugin.yc"),
				new StandScanResource(ResourceManager.getClassPath(AntContext.class)[0]+"**"),
				new StandScanResource(ResourceManager.getClassPath(ByteBufferedMessageChannel.class)[0]+"**"));
		ByteOutputStream bos = new ByteOutputStream();
		AtomicInteger counts = new AtomicInteger();
		ByteBufferChannel<AbstractMessage> byteBufferChannel = PlugsFactory.getPluginsInstance(ByteBufferChannel.class);
		
		BufferReady<AbstractMessage> messageWriteHandler = new BufferReady<AbstractMessage> (){
			@Override
			public void write(ByteBuffer buffer){
				counts.set(buffer.remaining());
				while(buffer.hasRemaining()) 
					bos.write(buffer.get());
				bos.reset();
			}
			@Override
			public void handleRead(ByteBuffer buffer) {
				int i = 0;
				System.out.println(counts);
				while(i<counts.get()) {
					buffer.put(bos.getBytes()[i]);
					i++;
				}
			}
			public void onMessage(AbstractMessage message) {
				System.out.println("得到新消息"+message);
			}
		};
		byteBufferChannel.setBufferReady(messageWriteHandler);
//		bos.write("hello world，im put".getBytes());
//		byteBufferChannel.handleRead();
		AbstractMessage message = new AbstractMessage();
		message.setMessage(new File("C:\\Users\\YaNan\\Documents\\WeChat Files\\tja414312570\\FileStorage\\File\\2021-07\\风险排查表.docx"));
		byteBufferChannel.write(message);
//		byteBufferChannel.handleRead(AbstractMessage.class);
		byteBufferChannel.write(message);
//		byteBufferChannel.handleRead(AbstractMessage.class);
		byteBufferChannel.write(message);
//		byteBufferChannel.handleRead(AbstractMessage.class);
		byteBufferChannel.write(message);
//		byteBufferChannel.handleRead(AbstractMessage.class);
		byteBufferChannel.write(message);
		byteBufferChannel.handleRead();
	}
}
