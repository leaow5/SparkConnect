package com.spark.core;

public class abstrackRunnable implements Runnable {

	private CallBack cb;
	private ReceiveMessage reOrder;

	/**
	 * 带参数函数.
	 * 
	 * @param arg1
	 *            CallBack
	 * @param arg2
	 *            ReceiveMessage
	 */
	public abstrackRunnable(CallBack arg1, ReceiveMessage arg2) {
		cb = arg1;
		reOrder = arg2;
	}

	/**
	 * 默认函数.
	 */
	public abstrackRunnable() {
		cb = null;
		reOrder = null;
	}

	@Override
	public void run() {
		if (cb != null) {
			cb.execute(reOrder);
		}
		
		return;
	}

}
