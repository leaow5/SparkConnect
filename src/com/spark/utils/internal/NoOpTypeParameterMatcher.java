package com.spark.utils.internal;

public class NoOpTypeParameterMatcher extends TypeParameterMatcher {
	@Override
	public boolean match(Object msg) {
		return true;
	}
}
