package com.spark.core;

public class abstrackRunnable implements Runnable {

	private CallBack cb;
	private String reOrder;

	public abstrackRunnable(CallBack arg1, String arg2) {
		cb = arg1;
		reOrder = arg2;
	}

	@Override
	public void run() {
		cb.execute(reOrder);
	}

}
