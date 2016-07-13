package com.spark.utils.internal;

import java.util.Iterator;

public final class ReadOnlyIterator<T> implements Iterator<T> {
	private final Iterator<? extends T> iterator;

	public ReadOnlyIterator(Iterator<? extends T> iterator) {
		if (iterator == null) {
			throw new NullPointerException("iterator");
		}
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		return iterator.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("read-only");
	}
}
