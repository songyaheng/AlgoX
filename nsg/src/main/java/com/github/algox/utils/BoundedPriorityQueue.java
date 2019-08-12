package com.github.algox.utils;

import java.io.Serializable;
import java.util.PriorityQueue;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 1:09 PM
 * @description:
 */
public class BoundedPriorityQueue<E> extends PriorityQueue<E> implements Serializable {
    private final int capacity;
    /**
     * Create a bounded priority queue with given maximum capacity.
     *
     * @param capacity The maximum capacity of the queue
     */
    public BoundedPriorityQueue(final int capacity) {
        super();
        this.capacity = capacity;
    }
    /**
     * Creates a priority queue with maximum capacity Integer.MAX_VALUE.
     */
    public BoundedPriorityQueue() {
        super();
        this.capacity = 0;
    }

    /**
     * When the queue is full, adds the element if it is larger than the
     * smallest element already in the queue.
     *
     * It the element is not comparable, throws a ClassCastException
     *
     * @param element
     * @return true if element was added
     */
    @Override
    public final boolean add(final E element) {
        if (!(element instanceof java.lang.Comparable)) {
            throw new ClassCastException();
        }

        if (this.contains(element)) {
            return false;
        }

        if (this.size() < capacity) {
            return super.add(element);
        }

        // Unlimited capacity (degenerate to classical PriorityQueue)
        // This is also required for correct deserialization because
        // elements are deserialized before the "capacity" field
        // => capacity is 0 when elements are added to the deserialized queue
        if (this.capacity == 0) {
            return super.add(element);
        }

        if (((Comparable) element).compareTo(this.peek()) > 0) {
            this.poll();
            return super.add(element);
        }

        return false;
    }

    /**
     *
     * @return
     */
    public final int getCapacity() {
        return capacity;
    }

    /**
     *
     * @param other
     * @return
     */
    @Override
    public final boolean equals(final Object other) {
        if (other == null) {
            return false;
        }

        if (!other.getClass().isInstance(this)) {
            return false;
        }

        BoundedPriorityQueue<E> otherQueue = (BoundedPriorityQueue<E>) other;

        return this.containsAll(otherQueue) && otherQueue.containsAll(this);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }
}
