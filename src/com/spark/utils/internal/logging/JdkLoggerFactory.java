package com.spark.utils.internal.logging;

import java.util.logging.Logger;

public class JdkLoggerFactory extends InternalLoggerFactory {

	public static final InternalLoggerFactory INSTANCE = new JdkLoggerFactory();

	/**
	 * @deprecated Use {@link #INSTANCE} instead.
	 */
	@Deprecated
	public JdkLoggerFactory() {
	}

	@Override
	public InternalLogger newInstance(String name) {
		return new JdkLogger(Logger.getLogger(name));
	}
}
