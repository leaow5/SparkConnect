package com.spark.utils.internal;

import java.util.concurrent.atomic.AtomicReference;

public final class LinkedQueueAtomicNode<E> extends AtomicReference<LinkedQueueAtomicNode<E>> {
	 private static final long serialVersionUID = 2404266111789071508L;
	    private E value;
	    LinkedQueueAtomicNode() {
	    }
	    LinkedQueueAtomicNode(E val) {
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

	    public void soNext(LinkedQueueAtomicNode<E> n) {
	        lazySet(n);
	    }

	    public LinkedQueueAtomicNode<E> lvNext() {
	        return get();
	    }
}
