package com.spark.utils.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import sun.misc.Unsafe;

final public class UnsafeAtomicReferenceFieldUpdater<U, M> extends AtomicReferenceFieldUpdater<U, M> {
	private final long offset;
	private final Unsafe unsafe;

	UnsafeAtomicReferenceFieldUpdater(Unsafe unsafe, Class<? super U> tClass, String fieldName)
			throws NoSuchFieldException {
		Field field = tClass.getDeclaredField(fieldName);
		if (!Modifier.isVolatile(field.getModifiers())) {
			throw new IllegalArgumentException("Must be volatile");
		}
		this.unsafe = unsafe;
		offset = unsafe.objectFieldOffset(field);
	}

	@Override
	public boolean compareAndSet(U obj, M expect, M update) {
		return unsafe.compareAndSwapObject(obj, offset, expect, update);
	}

	@Override
	public boolean weakCompareAndSet(U obj, M expect, M update) {
		return unsafe.compareAndSwapObject(obj, offset, expect, update);
	}

	@Override
	public void set(U obj, M newValue) {
		unsafe.putObjectVolatile(obj, offset, newValue);
	}

	@Override
	public void lazySet(U obj, M newValue) {
		unsafe.putOrderedObject(obj, offset, newValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public M get(U obj) {
		return (M) unsafe.getObjectVolatile(obj, offset);
	}
}
