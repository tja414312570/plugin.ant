package com.yanan.framework.ant.interfaces;

import com.yanan.framework.ant.annotations.Ant;
import com.yanan.framework.plugin.annotations.Service;

@Ant
@Service
public interface AntService {
	String ping();
}