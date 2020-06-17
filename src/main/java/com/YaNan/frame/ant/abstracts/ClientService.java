package com.YaNan.frame.ant.abstracts;

import java.io.IOException;

import com.YaNan.frame.ant.model.AntProviderSummary;
import com.YaNan.frame.ant.service.AntRuntimeService;

public interface ClientService {
	/**
	 * 连接到目标服务器
	 * @param providerSummary
	 * @param host
	 * @param port
	 * @throws IOException 
	 */
	void clientService(AntProviderSummary providerSummary) throws IOException;

	AntRuntimeService getRuntimeService();
	/**
	 * 开启服务提供
	 */
	void startProvider();
	/**
	 * 获取服务端口
	 * @return
	 */
	int getServerPort();

}
