package com.spark.core;

import javax.swing.JComponent;

public class ComponentRepaintCallBack
		implements CallBack, Comparable<ComponentRepaintCallBack> {
	// 组件回调用途
	private JComponent component;
	private final int priority = 10;
	// 命令保存
	private String orderMessage;
	private CallBackState callBackState = CallBackState.MESSAGE_NOTREADY;

	public JComponent getComponent() {
		return component;
	}

	public void setComponent(JComponent component) {
		this.component = component;
	}

	public void setOrderMessage(String orderMessage) {
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
	public String getOrderMessage() {
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
	public int compareTo(ComponentRepaintCallBack o) {
		// 复写此方法进行任务执行优先级排序
		// return priority < o.priority ? 1 :
		// (priority > o.priority ? -1 : 0);
		if (priority < o.priority) {
			return -1;
		} else {
			if (priority > o.priority) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
