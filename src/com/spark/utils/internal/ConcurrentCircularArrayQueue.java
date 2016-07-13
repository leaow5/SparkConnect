package com.spark.utils.internal;

import java.util.AbstractQueue;
import java.util.Iterator;

abstract class ConcurrentCircularArrayQueue<E> extends ConcurrentCircularArrayQueueL0Pad<E> {
	protected static final int REF_BUFFER_PAD;
	private static final long REF_ARRAY_BASE;
	private static final int REF_ELEMENT_SHIFT;
	static {
		final int scale = PlatformDependent0.UNSAFE.arrayIndexScale(Object[].class);
		if (4 == scale) {
			REF_ELEMENT_SHIFT = 2;
		} else if (8 == scale) {
			REF_ELEMENT_SHIFT = 3;
		} else {
			throw new IllegalStateException("Unknown pointer size");
		}
		// 2 cache lines pad
		// TODO: replace 64 with the value we can detect
		REF_BUFFER_PAD = (64 * 2) / scale;
		// Including the buffer pad in the array base offset
		REF_ARRAY_BASE = PlatformDependent0.UNSAFE.arrayBaseOffset(Object[].class) + (REF_BUFFER_PAD * scale);
	}
	protected final long mask;
	// @Stable :(
	protected final E[] buffer;

	@SuppressWarnings("unchecked")
	public ConcurrentCircularArrayQueue(int capacity) {
		int actualCapacity = roundToPowerOfTwo(capacity);
		mask = actualCapacity - 1;
		// pad data on either end with some empty slots.
		buffer = (E[]) new Object[actualCapacity + REF_BUFFER_PAD * 2];
	}

	private static int roundToPowerOfTwo(final int value) {
		return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
	}

	/**
	 * @param index
	 *            desirable element index
	 * @return the offset in bytes within the array for a given index.
	 */
	protected final long calcElementOffset(long index) {
		return calcElementOffset(index, mask);
	}

	/**
	 * @param index
	 *            desirable element index
	 * @param mask
	 * @return the offset in bytes within the array for a given index.
	 */
	protected static final long calcElementOffset(long index, long mask) {
		return REF_ARRAY_BASE + ((index & mask) << REF_ELEMENT_SHIFT);
	}

	/**
	 * A plain store (no ordering/fences) of an element to a given offset
	 *
	 * @param offset
	 *            computed via
	 *            {@link ConcurrentCircularArrayQueue#calcElementOffset(long)}
	 * @param e
	 *            a kitty
	 */
	protected final void spElement(long offset, E e) {
		spElement(buffer, offset, e);
	}

	/**
	 * A plain store (no ordering/fences) of an element to a given offset
	 *
	 * @param buffer
	 *            this.buffer
	 * @param offset
	 *            computed via
	 *            {@link ConcurrentCircularArrayQueue#calcElementOffset(long)}
	 * @param e
	 *            an orderly kitty
	 */
	protected static final <E> void spElement(E[] buffer, long offset, E e) {
		PlatformDependent0.UNSAFE.putObject(buffer, offset, e);
	}

	/**
	 * An ordered store(store + StoreStore barrier) of an element to a given
	 * offset
	 *
	 * @param offset
	 *            computed via
	 *            {@link ConcurrentCircularArrayQueue#calcElementOffset(long)}
	 * @param e
	 *            an orderly kitty
	 */
	protected final void soElement(long offset, E e) {
		soElement(buffer, offset, e);
	}

	/**
	 * An ordered store(store + StoreStore barrier) of an element to a given
	 * offset
	 *
	 * @param buffer
	 *            this.buffer
	 * @param offset
	 *            computed via
	 *            {@link ConcurrentCircularArrayQueue#calcElementOffset(long)}
	 * @param e
	 *            an orderly kitty
	 */
	protected static final <E> void soElement(E[] buffer, long offset, E e) {
		PlatformDependent0.UNSAFE.putOrderedObject(buffer, offset, e);
	}

	/**
	 * A plain load (no ordering/fences) of an element from a given offset.
	 *
	 * @param offset
	 *            computed via
	 *            {@link ConcurrentCircularArrayQueue#calcElementOffset(long)}
	 * @return the element at the offset
	 */
	protected final E lpElement(long offset) {
		return lpElement(buffer, offset);
	}

	/**
	 * A plain load (no ordering/fences) of an element from a given offset.
	 *
	 * @param buffer
	 *            this.buffer
	 * @param offset
	 *            computed via
	 *            {@link ConcurrentCircularArrayQueue#calcElementOffset(long)}
	 * @return the element at the offset
	 */
	@SuppressWarnings("unchecked")
	protected static final <E> E lpElement(E[] buffer, long offset) {
		return (E) PlatformDependent0.UNSAFE.getObject(buffer, offset);
	}

	/**
	 * A volatile load (load + LoadLoad barrier) of an element from a given
	 * offset.
	 *
	 * @param offset
	 *            computed via
	 *            {@link ConcurrentCircularArrayQueue#calcElementOffset(long)}
	 * @return the element at the offset
	 */
	protected final E lvElement(long offset) {
		return lvElement(buffer, offset);
	}

	/**
	 * A volatile load (load + LoadLoad barrier) of an element from a given
	 * offset.
	 *
	 * @param buffer
	 *            this.buffer
	 * @param offset
	 *            computed via
	 *            {@link ConcurrentCircularArrayQueue#calcElementOffset(long)}
	 * @return the element at the offset
	 */
	@SuppressWarnings("unchecked")
	protected static final <E> E lvElement(E[] buffer, long offset) {
		return (E) PlatformDependent0.UNSAFE.getObjectVolatile(buffer, offset);
	}

	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		while (poll() != null || !isEmpty()) {
			// looping
		}
	}

	public int capacity() {
		return (int) (mask + 1);
	}
}

abstract class ConcurrentCircularArrayQueueL0Pad<E> extends AbstractQueue<E> {
	long p00, p01, p02, p03, p04, p05, p06, p07;
	long p30, p31, p32, p33, p34, p35, p36, p37;
}