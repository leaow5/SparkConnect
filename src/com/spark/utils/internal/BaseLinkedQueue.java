package com.spark.utils.internal;

import java.util.AbstractQueue;
import java.util.Iterator;

abstract class BaseLinkedQueue<E> extends BaseLinkedQueueConsumerNodeRef<E>{
	 long p01, p02, p03, p04, p05, p06, p07;
	    long p10, p11, p12, p13, p14, p15, p16, p17;

	    @Override
	    public final Iterator<E> iterator() {
	        throw new UnsupportedOperationException();
	    }

	    /**
	     * {@inheritDoc} <br>
	     * <p>
	     * IMPLEMENTATION NOTES:<br>
	     * This is an O(n) operation as we run through all the nodes and count them.<br>
	     *
	     * @see java.util.Queue#size()
	     */
	    @Override
	    public final int size() {
	        // Read consumer first, this is important because if the producer is node is 'older' than the consumer the
	        // consumer may overtake it (consume past it). This will lead to an infinite loop below.
	        LinkedQueueNode<E> chaserNode = lvConsumerNode();
	        final LinkedQueueNode<E> producerNode = lvProducerNode();
	        int size = 0;
	        // must chase the nodes all the way to the producer node, but there's no need to chase a moving target.
	        while (chaserNode != producerNode && size < Integer.MAX_VALUE) {
	            LinkedQueueNode<E> next;
	            while ((next = chaserNode.lvNext()) == null) {
	                continue;
	            }
	            chaserNode = next;
	            size++;
	        }
	        return size;
	    }

	    /**
	     * {@inheritDoc} <br>
	     * <p>
	     * IMPLEMENTATION NOTES:<br>
	     * Queue is empty when producerNode is the same as consumerNode. An alternative implementation would be to observe
	     * the producerNode.value is null, which also means an empty queue because only the consumerNode.value is allowed to
	     * be null.
	     *
	     * @see MessagePassingQueue#isEmpty()
	     */
	    @Override
	    public final boolean isEmpty() {
	        return lvConsumerNode() == lvProducerNode();
	    }

	    @Override
	    public int capacity() {
	        return UNBOUNDED_CAPACITY;
	    }
	}

	abstract class BaseLinkedQueuePad0<E> extends AbstractQueue<E> implements MessagePassingQueue<E> {
	    long p00, p01, p02, p03, p04, p05, p06, p07;
	    long p10, p11, p12, p13, p14, p15, p16;
	}

	abstract class BaseLinkedQueueProducerNodeRef<E> extends BaseLinkedQueuePad0<E> {
	    protected static final long P_NODE_OFFSET;

	    static {
	        try {
	            P_NODE_OFFSET = PlatformDependent0.objectFieldOffset(
	                    BaseLinkedQueueProducerNodeRef.class.getDeclaredField("producerNode"));
	        } catch (NoSuchFieldException e) {
	            throw new RuntimeException(e);
	        }
	    }
	    protected LinkedQueueNode<E> producerNode;
	    protected final void spProducerNode(LinkedQueueNode<E> node) {
	        producerNode = node;
	    }

	    @SuppressWarnings("unchecked")
	    protected final LinkedQueueNode<E> lvProducerNode() {
	        return (LinkedQueueNode<E>) PlatformDependent0.getObjectVolatile(this, P_NODE_OFFSET);
	    }

	    protected final LinkedQueueNode<E> lpProducerNode() {
	        return producerNode;
	    }
	}

	abstract class BaseLinkedQueuePad1<E> extends BaseLinkedQueueProducerNodeRef<E> {
	    long p01, p02, p03, p04, p05, p06, p07;
	    long p10, p11, p12, p13, p14, p15, p16, p17;
	}

	abstract class BaseLinkedQueueConsumerNodeRef<E> extends BaseLinkedQueuePad1<E> {
	    protected static final long C_NODE_OFFSET;

	    static {
	        try {
	            C_NODE_OFFSET = PlatformDependent0.objectFieldOffset(
	                    BaseLinkedQueueConsumerNodeRef.class.getDeclaredField("consumerNode"));
	        } catch (NoSuchFieldException e) {
	            throw new RuntimeException(e);
	        }
	    }
	    protected LinkedQueueNode<E> consumerNode;
	    protected final void spConsumerNode(LinkedQueueNode<E> node) {
	        consumerNode = node;
	    }

	    @SuppressWarnings("unchecked")
	    protected final LinkedQueueNode<E> lvConsumerNode() {
	        return (LinkedQueueNode<E>) PlatformDependent0.getObjectVolatile(this, C_NODE_OFFSET);
	    }

	    protected final LinkedQueueNode<E> lpConsumerNode() {
	        return consumerNode;
	    }
}
