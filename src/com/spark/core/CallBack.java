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
	 * 设置延迟时间.
	 * 
	 * @return Date
	 */
	Date getDelayTime();
	
	/**
	 * 字符集.
	 * @return boolean
	 */
	boolean getCharset();
	/**
	 * uuid.
	 */
	String getUuid();
}
