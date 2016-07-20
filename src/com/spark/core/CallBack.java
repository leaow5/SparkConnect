package com.spark.core;

import java.util.Date;

public interface CallBack {
	/**
	 * 执行回调方法
	 * 
	 * @param objects
	 *            将处理后的结果作为参数返回给回调方法
	 */
	void execute(Object... objects);

	/**
	 * 命令
	 * 
	 * @return
	 */
	byte[] getOrderMessage();

	/**
	 * 获取消息状态
	 * 
	 * @return
	 */
	CallBackState getCallBackState();

	/**
	 * 设置消息状态
	 * 
	 * @return
	 */
	void setCallBackState(CallBackState arg);
	
	/**
	 * 设置延迟时间.
	 * @return Date
	 */
	Date getDelayTime();
}
