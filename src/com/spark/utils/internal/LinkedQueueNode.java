package com.spark.utils.internal;

final class LinkedQueueNode<E> {

	private static final long NEXT_OFFSET;
	static {
		try {
			NEXT_OFFSET = PlatformDependent0.objectFieldOffset(LinkedQueueNode.class.getDeclaredField("next"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
	private E value;
	private volatile LinkedQueueNode<E> next;

	LinkedQueueNode() {
		this(null);
	}

	LinkedQueueNode(E val) {
		spValue(val);
	}

	/**
	 * Gets the current value and nulls out the reference to it from this node.
	 *
	 * @return value
	 */
	public E getAndNullValue() {
		E temp = lpValue();
		spValue(null);
		return temp;
	}

	public E lpValue() {
		return value;
	}

	public void spValue(E newValue) {
		value = newValue;
	}

	public void soNext(LinkedQueueNode<E> n) {
		PlatformDependent0.putOrderedObject(this, NEXT_OFFSET, n);
	}

	public LinkedQueueNode<E> lvNext() {
		return next;
	}
}
