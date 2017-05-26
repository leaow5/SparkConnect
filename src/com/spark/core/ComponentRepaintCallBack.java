package com.spark.core;

import java.util.Date;

import javax.swing.JComponent;

import com.alibaba.fastjson.annotation.JSONField;

public class ComponentRepaintCallBack extends abstractCallBack implements CallBack {
	// 组件回调用途
	@JSONField(serialize = false)
	private JComponent component;

	private Date delayDate = null;
	// 命令保存
	private byte[] orderMessage;

	public JComponent getComponent() {
		return component;
	}

	public void setComponent(JComponent component) {
		this.component = component;
	}

	public void setOrderMessage(byte[] orderMessage) {
		this.orderMessage = orderMessage;
	}

	public ComponentRepaintCallBack(JComponent arg) {
		component = arg;
	}

	@Override
	public void execute(Object... objects) {
		System.out.println(getOrderMessage());

	}

	@Override
	public byte[] getOrderMessage() {
		return orderMessage;
	}

	@Override
	public Date getDelayTime() {
		return delayDate;
	}

}
