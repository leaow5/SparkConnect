package com.spark.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServices {
	private ExecutorServices() {

	}

	private static ExecutorService sc = null;

	public static ExecutorService getExecutorServices() {
		if (sc == null) {
			synchronized (SerialConnecter.class) {
				if (sc == null) {
					sc = Executors.newFixedThreadPool(3);
				}
			}
		}
		return sc;
	}

}
