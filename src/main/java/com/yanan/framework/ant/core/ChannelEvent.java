package com.yanan.framework.ant.core;

import com.yanan.framework.plugin.event.AbstractEvent;

public enum ChannelEvent implements AbstractEvent{
	INITING,INIT,OPENING,OPEN,CLOSEING,CLOSE,READING,WRITEING, EXCEPTION;
}
