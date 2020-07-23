package com.yanan.frame.ant.interfaces;

import com.yanan.frame.ant.annotations.Ant;
import com.yanan.frame.plugin.annotations.Service;

@Ant
@Service
public interface AntService {
	String ping();
}