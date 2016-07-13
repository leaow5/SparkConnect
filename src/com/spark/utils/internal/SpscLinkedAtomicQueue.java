package com.spark.utils.internal;

public final class SpscLinkedAtomicQueue<E> extends BaseLinkedAtomicQueue<E> {
	public SpscLinkedAtomicQueue() {
		super();
		LinkedQueueAtomicNode<E> node = new LinkedQueueAtomicNode<E>();
		spProducerNode(node);
		spConsumerNode(node);
		node.soNext(null); // this ensures correct construction: StoreStore
	}

	/**
	 * {@inheritDoc} <br>
	 *
	 * IMPLEMENTATION NOTES:<br>
	 * Offer is allowed from a SINGLE thread.<br>
	 * Offer allocates a new node (holding the offered value) and:
	 * <ol>
	 * <li>Sets that node as the producerNode.next
	 * <li>Sets the new node as the producerNode
	 * </ol>
	 * From this follows that producerNode.next is always null and for all other
	 * nodes node.next is not null.
	 *
	 * @see MessagePassingQueue#offer(Object)
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(final E nextValue) {
		if (nextValue == null) {
			throw new IllegalArgumentException("null elements not allowed");
		}
		final LinkedQueueAtomicNode<E> nextNode = new LinkedQueueAtomicNode<E>(nextValue);
		lpProducerNode().soNext(nextNode);
		spProducerNode(nextNode);
		return true;
	}

	/**
	 * {@inheritDoc} <br>
	 *
	 * IMPLEMENTATION NOTES:<br>
	 * Poll is allowed from a SINGLE thread.<br>
	 * Poll reads the next node from the consumerNode and:
	 * <ol>
	 * <li>If it is null, the queue is empty.
	 * <li>If it is not null set it as the consumer node and return it's now
	 * evacuated value.
	 * </ol>
	 * This means the consumerNode.value is always null, which is also the
	 * starting point for the queue. Because null values are not allowed to be
	 * offered this is the only node with it's value set to null at any one
	 * time.
	 *
	 */
	@Override
	public E poll() {
		final LinkedQueueAtomicNode<E> nextNode = lpConsumerNode().lvNext();
		if (nextNode != null) {
			// we have to null out the value because we are going to hang on to
			// the node
			final E nextValue = nextNode.getAndNullValue();
			spConsumerNode(nextNode);
			return nextValue;
		}
		return null;
	}

	@Override
	public E peek() {
		final LinkedQueueAtomicNode<E> nextNode = lpConsumerNode().lvNext();
		if (nextNode != null) {
			return nextNode.lpValue();
		} else {
			return null;
		}
	}
}
