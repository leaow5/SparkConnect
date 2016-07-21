package com.spark.core;

import java.util.Date;

public class SynCallBack extends abstractCallBack {
	// 组件回调用途

	private Date delayDate = null;
	// 命令保存
	private byte[] orderMessage;
	private CallBackState callBackState = CallBackState.MESSAGE_NOTREADY;

	public void setOrderMessage(byte[] orderMessage) {
		this.orderMessage = orderMessage;
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
	public CallBackState getCallBackState() {

		return callBackState;
	}

	@Override
	public void setCallBackState(CallBackState arg) {
		callBackState = arg;
	}

	@Override
	public Date getDelayTime() {
		return delayDate;
	}

}
