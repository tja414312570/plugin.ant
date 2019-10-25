package com.YaNan.frame.ant.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Ant服务提供节点
 * @author yanan
 */
public class AntProviderNode {
	/**
	 * Ant服务名
	 */
	private String name;
	/**
	 * Ant服务列表
	 */
	private ConcurrentLinkedQueue<AntProviderContext> providerList = new ConcurrentLinkedQueue<AntProviderContext>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void addProvider(AntProviderContext context) {
		if(providerList.contains(context))
			return;
		providerList.add(context);
	}
	public List<AntProviderSummary> summaryList() {
		List<AntProviderSummary> list = new ArrayList<AntProviderSummary>(providerList.size());
		for(AntProviderContext context : providerList) {
			list.add(context.getProviderSummary());
		}
		return list;
	}
	public List<AntProviderContext> providerContextList() {
		List<AntProviderContext> list = new ArrayList<AntProviderContext>(providerList.size());
		for(AntProviderContext context : providerList) {
			list.add(context);
		}
		return list;
	}
	public void remove(AntProviderContext antProviderContext) {
		this.providerList.remove(antProviderContext);
	}
}
