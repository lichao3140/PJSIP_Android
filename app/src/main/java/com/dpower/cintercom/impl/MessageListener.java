package com.dpower.cintercom.impl;

// 定义接收到消息时的,处理消息的接口
public interface MessageListener {
	public void message(byte[] msg, String ip);
}
