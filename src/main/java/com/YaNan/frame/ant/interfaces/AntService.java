package com.YaNan.frame.ant.interfaces;

import com.YaNan.frame.ant.annotations.Ant;
import com.YaNan.frame.plugin.annotations.Service;

@Ant
@Service
public interface AntService {
	String ping();
}
