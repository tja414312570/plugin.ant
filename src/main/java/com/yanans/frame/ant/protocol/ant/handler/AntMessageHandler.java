package com.yanan.frame.ant.protocol.ant.handler;

import java.io.IOException;
import java.io.WriteAbortedException;
import java.lang.reflect.Field;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.yanan.frame.ant.AntContextConfigure;
import com.yanan.frame.ant.exception.AntMessageResolveException;
import com.yanan.frame.ant.exception.AntMessageSerialException;
import com.yanan.frame.ant.exception.AntMessageWriteException;
import com.yanan.frame.ant.handler.AntMeessageSerialHandler;
import com.yanan.frame.ant.interfaces.AntMessageSerialization;
import com.yanan.frame.ant.interfaces.BufferReady;
import com.yanan.frame.ant.model.AntMessagePrototype;
import com.yanan.frame.ant.service.AntRuntimeService;
import com.yanan.frame.ant.type.BufferType;
import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.frame.plugin.exception.PluginNotFoundException;
import com.yanan.utils.ByteUtils;

import sun.misc.Cleaner;

/**
 * 
 * RPC包 线程安全 此类用来处理 半包 粘包问题 需要遵循消息协议 消息 = 帧长+帧内容 ... 帧长+帧内容格式的帧 一个链接所有流对应一个RPC包
 * 帧：一个完整的数据
 * 
 * @author yanan
 *
 */
@SuppressWarnings("restriction")
public class AntMessageHandler{
	/**
	 * 用来存储缓存
	 */
	private LinkedList<AntMessagePrototype> messageList;
	/**
	 * 读buffer
	 */
	private ByteBuffer readBuffer;
	/**
	 * 写数据用的buffer
	 */
	private ByteBuffer writeBuffer;
	/**
	 * 调用信息的缓存
	 */
	private volatile static  Map<Integer,byte[]> invokeInfoCache = new HashMap<Integer,byte[]>();
	/**
	 * 包头数据大小
	 */
	private final int PACKAGE_HEAD_LENGTH = 12;
	/**
	 * 包头缓冲
	 */
	private final byte[] packageHeadBuffer = new byte[PACKAGE_HEAD_LENGTH];
	/**
	 * 默认包体缓冲大小
	 */
	private int bufferSize = 128;
	/**
	 * 包体缓冲
	 */
	private byte[] packageBodyBuffer = new byte[bufferSize];
	/**
	 * 缓冲最大值
	 */
	private int maxBufferSize = 1024;
	/**
	 * 溢出包长度
	 */
	private int outflowPackageLen;
	/**
	 * 序列化工具
	 */
	private AntMessageSerialization serailzationHandler;
	public AntMessageHandler(AntRuntimeService runtimeService) {
		messageList = new LinkedList<AntMessagePrototype>();
		AntContextConfigure config = runtimeService.getContextConfigure();
		setMaxBufferSize(config.getBufferMaxSize());
		if (config.getBufferType() == BufferType.DIRECT) {
			setReadBuffer(ByteBuffer.allocateDirect(config.getBufferSize()));
			setWriteBuffer(ByteBuffer.allocateDirect(config.getBufferSize()));
		} else {
			setReadBuffer(ByteBuffer.allocate(config.getBufferSize()));
			setWriteBuffer(ByteBuffer.allocate(config.getBufferSize()));
		}
		try {
			this.serailzationHandler = PlugsFactory.getPluginsInstance(AntMessageSerialization.class);
		}catch(PluginNotFoundException e) {
			PlugsFactory.getInstance().addPlugininDefinition(AntMeessageSerialHandler.class);
			this.serailzationHandler = PlugsFactory.getPluginsInstance(AntMessageSerialization.class);
		}
	}

	
	public AntMessageSerialization getSerailzationHandler() {
		return serailzationHandler;
	}
	public void setSerailzationHandler(AntMessageSerialization serailzationHandler) {
		this.serailzationHandler = serailzationHandler;
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
	 * 判断是否还有更多帧
	 * 
	 * @return
	 */
	public boolean hasMoreMessage() {
		return !this.messageList.isEmpty();
	}

	/**
	 * 获取帧数量
	 * 
	 * @param pos
	 * @return
	 */
	public int getMessageNum() {
		return this.messageList.size();
	}

	/**
	 * 读取第一帧并返回一个对象
	 * 
	 * @param msg
	 * @param type
	 * @return
	 */
	public synchronized AntMessagePrototype getMessage() {
		if (messageList.isEmpty())
			return null;
		return messageList.removeFirst();
	}

	public void handleSocketChannel(SocketChannel socketChannel) {

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
	public synchronized void handleRead() throws AntMessageResolveException{
		boolean compact = false;
		// 从通道读取数据
			try {
//				System.out.println("读取:"+Arrays.toString(readBuffer.array()));
				readBuffer.flip();
//				int limit = readBuffer.limit();
				// 缓冲区可用数据长度
				int available;
				while ((available = readBuffer.remaining()) > PACKAGE_HEAD_LENGTH) {
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
//					System.out.println("可用数量:"+available);
					// 读取包头
					readBuffer.get(packageHeadBuffer);
					// 获取服务名长度
					int serviceLen = packageHeadBuffer[0] & 0xff;
					// 获取消息头长度
					int infoLen = ByteUtils.byteToUnsignedShort(packageHeadBuffer, 5);
					// 消息体长度
					int bodyLen = ByteUtils.byteToInt(packageHeadBuffer, 7);
					// 总长度
					int messageLen = serviceLen + infoLen + bodyLen;
					// 获取请求号
					int RID = ByteUtils.byteToInt(packageHeadBuffer, 1);
					if (messageLen+PACKAGE_HEAD_LENGTH > this.maxBufferSize) {
//						readBuffer.limit(0);
						readBuffer.position(readBuffer.limit());
//						readBuffer.get(packageBodyBuffer, PACKAGE_HEAD_LENGTH, messageLen);
						this.outflowPackageLen = messageLen-available+PACKAGE_HEAD_LENGTH;
//						System.out.println( "request package len (" + messageLen
//								+ ") out of configure max buffer size (" + maxBufferSize + ")");
//						break;
						throw new AntMessageResolveException("request package len (" + messageLen
								+ ") out of configure max buffer size (" + maxBufferSize + ")", RID);
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
							//计算需要扩容的容量
							int len = calculateCapacity(readBuffer.capacity(),messageLen << 1,this.maxBufferSize);
							//扩容
							this.readBuffer = ensureCapacity(this.readBuffer,len);
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
					// 判断包体缓冲长度是否小于消息体长度
					if (packageBodyBuffer.length < messageLen) {
						int len = calculateCapacity(packageBodyBuffer.length,messageLen,this.maxBufferSize);
						packageBodyBuffer = new byte[len];
					}
					// 将消息体读入缓冲数组
					readBuffer.get(packageBodyBuffer, 0, messageLen);
//					System.out.println("包内容:"+new String(packageBodyBuffer));
					// 获取服务名
					String serviceName = new String(packageBodyBuffer, 0, serviceLen);
					// 获取消息类型
					int messageType = packageHeadBuffer[11];
					// 请求内容长度
					int mpLen = infoLen + bodyLen;
					// 将消息体缓存起来
					byte[] cache = new byte[mpLen];
					System.arraycopy(packageBodyBuffer, serviceLen, cache, 0, mpLen);
//					System.out.println("体内容:"+new String(cache));
					AntMessagePrototype message = new AntMessagePrototype();
					message.setRID(RID);
					message.setService(serviceName);
					message.setType(messageType);
					message.setBuffered(cache);
					message.setInvokeHeaderLen(infoLen);
					messageList.add(message);
//					System.out.println(message);
				}
			} finally {
				if(!compact)
					readBuffer.compact();
//					if(this.outflowPackageLen > 0) {
//						this.readBuffer.clear();
//					}
			}
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
				throw new IllegalArgumentException("the current  size("+size+") over flow the max size("+minBufferSize+")");
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
	public synchronized void write(AntMessagePrototype message,BufferReady bufferedOverflow){
		// 整个消息 占12 + serviceLen + invokeInfoLen + bodyLen > 12
		byte[] infoHead = new byte[12];
		// 服务名长度
		infoHead[0] = (byte) (message.getService() == null ? 0 : message.getService().getBytes().length);
		// 请求号长度 4
		System.arraycopy(ByteUtils.intToByte(message.getRID()), 0, infoHead, 1, 4);
		// 调用信息内容
		byte[] invokeInfoBytes = getInvokeInfoBytes(message);
		// 如果有请求的类信息
		int invokeInfoLen = invokeInfoBytes == null ? 0 : invokeInfoBytes.length;
		if (invokeInfoLen > Short.MAX_VALUE) {
			// 如果长度超过Short的总大小 抛出异常 并返回
			if (invokeInfoLen > Short.MAX_VALUE << 2) {
				throw new AntMessageSerialException("message info overflow,desc:" + message, message);
			}
			invokeInfoLen = invokeInfoLen & Short.MAX_VALUE;
		}
		//消息体:体数量+体长度+体内容
		byte[] invokeParameter = null;
		if (message.getInvokeParmeters() != null) {
			ByteBuffer serialBuffer = ByteBuffer.allocateDirect(2048);
			//获取参数数量
			//将写指针右移至包长位置
			serialBuffer.put((byte) message.getInvokeParmeters().length);
			for(int i = 0;i<message.getInvokeParmeters().length;i++) {
				ByteBuffer byteBuffer = serailzationHandler.serialAntMessage(message.getInvokeParmeters()[i], message);
				int len = 0;
				if(byteBuffer!=null) {
					byteBuffer.flip();
					len = byteBuffer.remaining();
					serialBuffer.put(ByteUtils.intToByte(len));
					//写入参数内容
					while(byteBuffer != null && byteBuffer.hasRemaining()) {
						serialBuffer.put(byteBuffer.get());
					}
					byteBuffer.clear();
				}else {
					serialBuffer.put(ByteUtils.intToByte(len));
				}
				serailzationHandler.clear();
			}
			serialBuffer.flip();
			if(serialBuffer.hasRemaining()) {
				invokeParameter = new byte[serialBuffer.remaining()];
				serialBuffer.get(invokeParameter);
			}
		}
		// 调用信息长度
		int bodyLen = invokeParameter == null ? 0 :invokeParameter.length;
		if (message.getBuffered() != null) {
			// 写入消息信息长度 占2位
			System.arraycopy(ByteUtils.unsignedShortToByte(message.getInvokeHeaderLen()), 0, infoHead, 5, 2);
			// 写入消息体长度 占4位
			System.arraycopy(ByteUtils.intToByte(message.getBuffered().length - message.getInvokeHeaderLen()), 0,
					infoHead, 7, 4);
		} else {
			// 写入消息信息长度 占2位
			System.arraycopy(ByteUtils.unsignedShortToByte(invokeInfoLen), 0, infoHead, 5, 2);
			// 写入消息体长度 占4位
			System.arraycopy(ByteUtils.intToByte(bodyLen), 0, infoHead, 7, 4);
		}
		//计算总长度
//		System.out.println(PACKAGE_HEAD_LENGTH+bodyLen+invokeInfoLen+infoHead[0]);
		infoHead[11] = (byte) message.getType();
		// 写入消息类型
		try {
			ensureRemaining(writeBuffer,bufferedOverflow,infoHead);
//		out.write(infoHead);
			// 写入服务名 占serviceLen位
			if (message.getService() != null)
				ensureRemaining(writeBuffer,bufferedOverflow,message.getService().getBytes());
			// 如果缓存不为空 写入消息体
			if (message.getBuffered() != null) {
				ensureRemaining(writeBuffer,bufferedOverflow,message.getBuffered());
			} else {
				// 写入消息体长度 占bodyLen位
				ensureRemaining(writeBuffer,bufferedOverflow,invokeInfoBytes);
				ensureRemaining(writeBuffer,bufferedOverflow,invokeParameter);
			}
			bufferedOverflow.bufferReady(writeBuffer);
		} catch (WriteAbortedException e) {
			throw new AntMessageWriteException(e,message);
		}
	}
	/**
	 * 获取调用信息
	 * @param message
	 * @return
	 */
	private static byte[] getInvokeInfoBytes(AntMessagePrototype message) {
		byte[] invokeInfoBytes;
		if(message.getInvokeClass() == null)
			return null;
		int hash = getInvokeHash(message);
		invokeInfoBytes = invokeInfoCache.get(hash);
		if(invokeInfoBytes == null) {
			synchronized (invokeInfoCache) {
				invokeInfoBytes = invokeInfoCache.get(hash);
				if(invokeInfoBytes == null) {
					StringBuilder invokeInfo;
					invokeInfo = new StringBuilder(message.getInvokeClass().getName());
					if (message.getInvokeMethod() != null) {
						invokeInfo.append(":").append(message.getInvokeMethod().getName());
						for (Class<?> parameterType : message.getInvokeMethod().getParameterTypes())
							invokeInfo.append(":").append(parameterType.getName());
					}
					invokeInfoBytes = invokeInfo.toString().getBytes();
					invokeInfoCache.put(hash, invokeInfoBytes);
				}
			}
		}
		return invokeInfoBytes;
	}
	/**
	 * 获取调用信息的hash值
	 * @param message
	 * @return
	 */
	private static int getInvokeHash(AntMessagePrototype message) {
		int hash = message.getInvokeClass().hashCode();
		if(message.getInvokeMethod()!=null)
			hash += message.getInvokeMethod().hashCode();
		return hash;
	}
	/**
	 * 确保数据正常写入
	 * @param byteBuffer
	 * @param bufferedOverflow
	 * @param contents
	 * @throws WriteAbortedException
	 */
	private void ensureRemaining(ByteBuffer byteBuffer,BufferReady bufferedOverflow, byte[] contents) throws WriteAbortedException {
		if(contents==null)
			return;
		if(bufferedOverflow != null) {
			int remaining =  byteBuffer.remaining();
			int len = contents.length;
			if(len > remaining) {
				int pos = 0;
				while(true) {
					bufferedOverflow.bufferReady(byteBuffer);
					remaining =  byteBuffer.remaining();
					if(remaining <=0 && len>0)
						throw new BufferOverflowException();
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
		}
		byteBuffer.put(contents);
	}

	public int getMaxBufferSize() {
		return maxBufferSize;
	}
	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}
	public int getBufferSize() {
		return bufferSize;
	}
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}