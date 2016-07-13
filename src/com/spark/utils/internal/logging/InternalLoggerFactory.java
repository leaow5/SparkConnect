package com.spark.utils.internal.logging;

import java.util.concurrent.ThreadLocalRandom;

public abstract class InternalLoggerFactory {
	private static volatile InternalLoggerFactory defaultFactory = newDefaultFactory(
			InternalLoggerFactory.class.getName());

	static {
		// Initiate some time-consuming background jobs here,
		// because this class is often initialized at the earliest time.
		try {
			Class.forName(ThreadLocalRandom.class.getName(), true, InternalLoggerFactory.class.getClassLoader());
		} catch (Exception ignored) {
			// Should not fail, but it does not harm to fail.
		}
	}

	@SuppressWarnings("UnusedCatchParameter")
	private static InternalLoggerFactory newDefaultFactory(String name) {
		InternalLoggerFactory f;
		try {
			f = Log4J2LoggerFactory.INSTANCE;
			f.newInstance(name).debug("Using Log4J as the default logging framework");
		} catch (Throwable t1) {

			f = JdkLoggerFactory.INSTANCE;
			f.newInstance(name).debug("Using java.util.logging as the default logging framework");

		}
		return f;
	}

	/**
	 * Returns the default factory. The initial default factory is
	 * {@link JdkLoggerFactory}.
	 */
	public static InternalLoggerFactory getDefaultFactory() {
		return defaultFactory;
	}

	/**
	 * Changes the default factory.
	 */
	public static void setDefaultFactory(InternalLoggerFactory defaultFactory) {
		if (defaultFactory == null) {
			throw new NullPointerException("defaultFactory");
		}
		InternalLoggerFactory.defaultFactory = defaultFactory;
	}

	/**
	 * Creates a new logger instance with the name of the specified class.
	 */
	public static InternalLogger getInstance(Class<?> clazz) {
		return getInstance(clazz.getName());
	}

	/**
	 * Creates a new logger instance with the specified name.
	 */
	public static InternalLogger getInstance(String name) {
		return getDefaultFactory().newInstance(name);
	}

	/**
	 * Creates a new logger instance with the specified name.
	 */
	protected abstract InternalLogger newInstance(String name);
}
