package com.spark.utils.internal;

public interface LongCounter {
	void add(long delta);

	void increment();

	void decrement();

	long value();
}
