package com.yanan.framework.ant.channel.buffer;

import java.io.IOException;
import java.io.WriteAbortedException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.yanan.framework.ant.core.BufferReady;
import com.yanan.framework.ant.core.ByteBufferChannel;
import com.yanan.framework.ant.core.MessageSerialization;
import com.yanan.framework.ant.exception.AntMessageResolveException;
import com.yanan.framework.ant.type.BufferType;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Service;
import com.yanan.framework.plugin.autowired.enviroment.Variable;
import com.yanan.utils.ByteUtils;

import sun.misc.Cleaner;

/**
 * 封装消息协议帧+
 * RPC包 线程安全 此类用来处理 半包 粘包问题 需要遵循消息协议 消息 = 帧长+帧内容 ... 帧长+帧内容格式的帧 一个链接所有流对应一个RPC包
 * 帧：一个完整的数据
 * 
 * @author yanan
 *
 */
@SuppressWarnings("restriction")
@Register(signlTon = false)
public class ByteBufferChannelHandler<T> implements ByteBufferChannel<T>{
	
	@Service
	private Logger log;
	/**
	 * 读buffer
	 */
	private ByteBuffer readBuffer;
	/**
	 * 写数据用的buffer
	 */
	private ByteBuffer writeBuffer;
	/**
	 * 包头数据大小
	 */
	private final int PACKAGE_HEAD_LENGTH = 4;
	/**
	 * 包头缓冲
	 */
	private final byte[] packageHeadBuffer = new byte[PACKAGE_HEAD_LENGTH];
	/**
	 * 缓冲最大值
	 */
	@Variable(value="ant.buffer.max",defaultValue = "102400")
	private int maxBufferSize;
	@Variable(value="ant.buffer.size",defaultValue = "2048")
	private int bufferSize;
	@Variable(value="ant.buffer.type",defaultValue = "DIRECT")
	private BufferType bufferType;
	/**
	 * 溢出包长度
	 */
	private int outflowPackageLen;
	/**
	 * 序列化工具
	 */
	@Service
	private MessageSerialization serailzation;
	private BufferReady<T> bufferReady;
	
	@PostConstruct
	public void init() {
		log.debug("byte buffer size:"+this.bufferSize);
		log.debug("byte buffer max size:"+this.maxBufferSize);
		log.debug("byte buffer type:"+this.bufferType);
		if (bufferType == BufferType.DIRECT) {
			setReadBuffer(ByteBuffer.allocateDirect(bufferSize));
			setWriteBuffer(ByteBuffer.allocateDirect(bufferSize));
		} else {
			setReadBuffer(ByteBuffer.allocate(bufferSize));
			setWriteBuffer(ByteBuffer.allocate(bufferSize));
		}
	}
	public int getOutflowPackageLen() {
		return outflowPackageLen;
	}
	public void setOutflowPackageLen(int outflowPackageLen) {
		this.outflowPackageLen = outflowPackageLen;
	}
	public void setBuffer(ByteBuffer readBuffer,ByteBuffer writeBuffer) {
		this.readBuffer = readBuffer;
		this.writeBuffer = writeBuffer;
	}
	public ByteBuffer getWriteBuffer() {
		return writeBuffer;
	}
	public void setWriteBuffer(ByteBuffer writeBuffer) {
		this.writeBuffer = writeBuffer;
	}
	public void setReadBuffer(ByteBuffer readBuffer) {
		this.readBuffer = readBuffer;
	}
	/**
	 * 获取包中的缓存类容
	 * 
	 * @return
	 */
	public ByteBuffer getReadBuffer() {
		return this.readBuffer;
	}

	/**
	 * 将输入流添加到包中
	 * 
	 * @param key
	 * 
	 * @param is
	 * @throws AntMessageResolveException 
	 * @throws IOException
	 */
	public void handleRead() {
		try {
			this.bufferReady.handleRead(readBuffer);
		}finally {
			read();
		}
	}
	private void read() {
		boolean compact = false;
		// 从通道读取数据
			try {
				readBuffer.flip();
				// 缓冲区可用数据长度
				int available;
				while ((available = readBuffer.remaining()) > PACKAGE_HEAD_LENGTH) {
					System.err.println("可用:"+available+Thread.currentThread());
					int position = readBuffer.position();
					if(this.outflowPackageLen > 0) {
						if(available>this.outflowPackageLen) {
							readBuffer.position(this.outflowPackageLen);
						}else {
							this.outflowPackageLen = this.outflowPackageLen - available;
							readBuffer.position(readBuffer.limit());
							break;
						}
					}
					// 读取包头
					readBuffer.get(packageHeadBuffer);
					// 消息体长度
					int messageLen = ByteUtils.bytesToInt(packageHeadBuffer);
					// 总长度
					if (messageLen+PACKAGE_HEAD_LENGTH > this.maxBufferSize) {
//						readBuffer.position(readBuffer.limit());
//						this.outflowPackageLen = messageLen-available+PACKAGE_HEAD_LENGTH;
//						System.err.println("扩容:"+messageLen);
//						readBuffer = ensureCapacity(writeBuffer, messageLen);
						throw new AntMessageResolveException("request package len (" + messageLen
								+ ") out of configure max buffer size (" + maxBufferSize + ")");
					}
					// 判断有效内容长度是否小于总长度
					if (available - PACKAGE_HEAD_LENGTH < messageLen) {
						// 有效内容小于总长度 半包情况
						//恢复指针位置
						readBuffer.position(position);
						//如果剩余容量小于总容量的1/3,扩容
						if(this.readBuffer.capacity() / 3 > this.readBuffer.capacity()-this.readBuffer.limit()) {
							//先压缩
							compress(readBuffer);
							if(this.readBuffer.capacity() / 3 > this.readBuffer.capacity()-this.readBuffer.limit()) {
								//计算需要扩容的容量
								int len = calculateCapacity(readBuffer.capacity(),messageLen << 1,this.maxBufferSize);
								//扩容
								this.readBuffer = ensureCapacity(this.readBuffer,len);
							}
						}
						//如果容量剩余小于包体长度时，也扩容
						if(messageLen > this.readBuffer.capacity()-this.readBuffer.limit()) {
							compress(readBuffer);
							int len = calculateCapacity(readBuffer.capacity(),messageLen << 1,this.maxBufferSize);
							this.readBuffer = ensureCapacity(this.readBuffer,len);
						}
						//如果指针位置太高、压缩一下
						if(this.readBuffer.capacity()-position < messageLen+PACKAGE_HEAD_LENGTH) {
							compress(readBuffer);
						}
						break;
					}
	 				//反序列化  
					int serialPosition = readBuffer.position();
					int serialLimit = readBuffer.limit();
					T message = serailzation.deserial(readBuffer, serialPosition, serialPosition+messageLen,null);
//					System.out.println((serialPosition+messageLen)+"==>"+available+"==>"+readBuffer.position()+"==>"+readBuffer.limit());
//							new TypeToken<T>() {}.getTypeClass());
					readBuffer.limit(serialLimit);
					System.err.println("读取:"+message);
					this.bufferReady.onMessage(message);
//					messageList.add(message);
				}
			} finally {
				if(!compact)
					readBuffer.compact();
//					if(this.outflowPackageLen > 0) {
//						this.readBuffer.clear();
//					}
			}
			//通知消息
//			T message;
//			while((message = getMessage()) != null) {
//				this.bufferReady.onMessage(message);
//			}
	}
	/**
	 * 压缩包
	 * @param byteBuffer
	 */
	public static void compress(ByteBuffer byteBuffer) {
		int pos = 0;
		int available = byteBuffer.remaining();
		while(byteBuffer.hasRemaining()) {
			byteBuffer.put(pos++,byteBuffer.get());
		}
		byteBuffer.position(0);
		byteBuffer.limit(available);
//		byteBuffer.compact();
	}
	/**
	 * 扩容
	 * @param byteBuffer
	 * @param len
	 * @return
	 */
	public static ByteBuffer ensureCapacity(ByteBuffer byteBuffer, int len) {
		if(byteBuffer.capacity()>=len)
			return byteBuffer;
		ByteBuffer  newBuffer = 
				byteBuffer.isDirect()?
						ByteBuffer.allocateDirect(len):ByteBuffer.allocate(len);
		int pos = byteBuffer.position();
		int limit = byteBuffer.limit();
		byteBuffer.rewind();
		while(byteBuffer.hasRemaining()) {
			byte[] byts = new byte[byteBuffer.remaining()];
			byteBuffer.get(byts);
			newBuffer.put(byts);
		}
		newBuffer.position(pos);
		newBuffer.limit(limit);
		clean(byteBuffer);
		return newBuffer;
	}
	/**
	 * 清理缓存
	 * @param byteBuffer
	 */
	private static void clean(ByteBuffer byteBuffer) {
		if(byteBuffer.isDirect()) {
			Field cleanerField;
			try {
				cleanerField = byteBuffer.getClass().getDeclaredField("cleaner");
				 cleanerField.setAccessible(true);
				    Cleaner cleaner = (Cleaner) cleanerField.get(byteBuffer);
				    cleaner.clean();
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}else {
			byteBuffer.clear();
		}
	}
	/**
	 * 计算容量
	 * @param size
	 * @param minBufferSize
	 * @param maxBufferSize
	 * @return
	 */
	public static int calculateCapacity(int size, int minBufferSize,int maxBufferSize) {
		if(size < 0 || minBufferSize < 0 || maxBufferSize < 0)
			throw new IllegalArgumentException("size are not allowed to be negative");
		if(maxBufferSize > 0) {
			if(minBufferSize > maxBufferSize)
				throw new IllegalArgumentException("need buffered size("+minBufferSize+") over flow the max size("+maxBufferSize+")");
			if(size > maxBufferSize)
				throw new IllegalArgumentException("the current  size("+size+") over flow the max size("+maxBufferSize+")");
		}
//		if(minBufferSize > 0 && size > minBufferSize)
//			throw new IllegalArgumentException("the current  size("+size+") over flow the min size("+minBufferSize+")");
		if(minBufferSize < size) {
			minBufferSize = size << 1;
			if(maxBufferSize > 0 && minBufferSize > maxBufferSize)
				minBufferSize = maxBufferSize;
		}
		if(size == 0)
			size = minBufferSize;
		while(size < minBufferSize) {
			size = size << 1;
			if(maxBufferSize > 0) {
				if (size > maxBufferSize || size < 0) {
					size = maxBufferSize;
				}
			}else {
				if(size < 0) {
					size = minBufferSize;
				}
			}
		}
		return size;
	}
	/**
	 * 写入数据
	 * @param message
	 * @param bufferedOverflow
	 */
	public synchronized void write(T message){
		if(message == null)
			return;
		byte[] infoHead = new byte[4];
		ByteBuffer messageBuffer = serailzation.serial(message);
		int messageLen = messageBuffer.remaining();
		
		if(this.writeBuffer.limit() < messageLen  || this.writeBuffer.capacity() / 3 > this.writeBuffer.capacity()-this.writeBuffer.limit()) {
			//先压缩
			compress(writeBuffer);
			if(this.writeBuffer.limit() < messageLen  || this.writeBuffer.capacity() / 3 > this.writeBuffer.capacity()-this.writeBuffer.limit()) {
				//计算需要扩容的容量
				int need = messageLen << 1;
				if(need>this.maxBufferSize)
					need = this.maxBufferSize;
				int len = calculateCapacity(writeBuffer.capacity(),need,this.maxBufferSize);
				//扩容
				this.writeBuffer = ensureCapacity(this.writeBuffer,len);
			}
		}
		System.err.println("计算容量"+writeBuffer.capacity()+"==>"+messageLen);
		infoHead = ByteUtils.intToBytes(messageLen);
		ensureRemaining(writeBuffer, infoHead);
		System.err.println("写入"+message+Thread.currentThread());
		while(messageBuffer.hasRemaining()) {
			if(!writeBuffer.hasRemaining())
				write(writeBuffer);
			writeBuffer.put(messageBuffer.get());
		}
		
		write(writeBuffer);
	}
	public void write(ByteBuffer buffer) {
		try {
			buffer.mark();
			buffer.flip();
			bufferReady.write(buffer);
		}finally {
			buffer.flip();
			buffer.clear();
//			buffer.compact();
		}
	}
	/**
	 * 确保数据正常写入
	 * @param byteBuffer
	 * @param bufferedOverflow
	 * @param contents
	 * @throws WriteAbortedException
	 */
	private void ensureRemaining(ByteBuffer byteBuffer, byte[] contents) {
		if(contents==null)
			return;
		int remaining =  byteBuffer.remaining();
		int len = contents.length;
		if(len > remaining) {
			int pos = 0;
			while(true) {
				write(byteBuffer);
				remaining =  byteBuffer.remaining();
				if(remaining <=0 && len>0)
					write(byteBuffer);
				int wLen = len-pos;
				if(wLen > remaining) 
					wLen = remaining;
				if(wLen==0) 
					break;
				byteBuffer.put(contents,pos,wLen);
				pos += wLen;
			}
			return;
		}
		byteBuffer.put(contents);
	}

	public int getMaxBufferSize() {
		return maxBufferSize;
	}
	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

	@Override
	public void setBufferReady(BufferReady<T> bufferReady) {
		this.bufferReady = bufferReady;
	} 
}