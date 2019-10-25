package com.YaNan.frame.ant;

import java.io.File;

import com.YaNan.frame.ant.handler.BufferTypeDecoder;
import com.YaNan.frame.ant.type.BufferType;
import com.YaNan.frame.utils.config.Decoder;
import com.YaNan.frame.utils.config.Self;
import com.typesafe.config.Config;

public class AntContextConfigure {
	/**
	 * 端口
	 */
	private int port;
	/**
	 * 地址
	 */
	private String host;
	/**
	 * 配置对象
	 */
	@Self
	private Config config;
	/**
	 * 超时
	 */
	private int timeout = 3000;
	/**
	 * 配置文件对象
	 */
	private File file;
	/**
	 * 内存类型
	 */
	@Decoder(BufferTypeDecoder.class)
	private BufferType bufferType = BufferType.HEAP;
	/**
	 * 内存大小
	 */
	private int bufferSize = 2048;
	/**
	 * 最大内存
	 */
	private int bufferMaxSize = 204800;
	/**
	 * 注册检测时间间隔
	 */
	private int checkTime = 100;
	/**
	 * 消息处理线程数
	 */
	private int process = 0;
	/**
	 * 消息处理最大线程数
	 */
	private int maxProcess = 0;
	/**
	 * 作为服务端时的名字
	 */
	private String name;
	/**
	 * 作为服务端时的端口
	 */
	private int serverPort;
	/**
	 * 心跳检查时间
	 */
	private int interval = 5000;
	/**
	 * 线程池处理线程的最大任务队列数量
	 */
	private int tasksize = 1024;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public BufferType getBufferType() {
		return bufferType;
	}

	public void setBufferType(BufferType bufferType) {
		this.bufferType = bufferType;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(int checkTime) {
		this.checkTime = checkTime;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}


	public int getProcess() {
		return process;
	}
	
	public void setProcess(int process) {
		this.process = process;
	}



	@Override
	public String toString() {
		return "AntContextConfigure [port=" + port + ", host=" + host + ", config=" + config + ", timeout=" + timeout
				+ ", file=" + file + ", bufferType=" + bufferType + ", bufferSize=" + bufferSize + ", bufferMaxSize="
				+ bufferMaxSize + ", checkTime=" + checkTime + ", process=" + process + ", name=" + name
				+ ", serverPort=" + serverPort + ", interval=" + interval + "]";
	}

	public int getBufferMaxSize() {
		return bufferMaxSize;
	}

	public void setBufferMaxSize(int bufferMaxSize) {
		this.bufferMaxSize = bufferMaxSize;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getMaxProcess() {
		return maxProcess;
	}

	public void setMaxProcess(int maxProcess) {
		this.maxProcess = maxProcess;
	}

	public int getTasksize() {
		return tasksize;
	}

	public void setTasksize(int tasksize) {
		this.tasksize = tasksize;
	}
}
