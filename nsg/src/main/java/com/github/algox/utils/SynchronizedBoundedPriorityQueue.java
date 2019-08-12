package com.github.algox.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 1:08 PM
 * @description:
 */
public class SynchronizedBoundedPriorityQueue<E> implements Serializable, Iterable<E>, Collection<E>, Queue<E> {
    private final BoundedPriorityQueue<E> queue;

    public SynchronizedBoundedPriorityQueue(final int capacity) {
        queue = new BoundedPriorityQueue<E>(capacity);
    }

    @Override
    public final Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public final int size() {
        synchronized (this) {
            return queue.size();
        }
    }

    @Override
    public final boolean offer(final E e) {
        synchronized (this) {
            return queue.offer(e);
        }
    }

    @Override
    public final E poll() {
        synchronized (this) {
            return queue.poll();
        }
    }

    @Override
    public final E peek() {
        synchronized (this) {
            return queue.peek();
        }
    }

    @Override
    public final boolean isEmpty() {
        synchronized (this) {
            return queue.isEmpty();
        }
    }

    @Override
    public final boolean contains(final Object o) {
        synchronized (this) {
            return queue.contains(o);
        }
    }

    @Override
    public final Object[] toArray() {
        synchronized (this) {
            return queue.toArray();
        }
    }

    @Override
    public final <T> T[] toArray(final T[] a) {
        synchronized (this) {
            return queue.toArray(a);
        }
    }

    @Override
    public final boolean add(final E e) {
        synchronized (this) {
            return queue.add(e);
        }
    }

    @Override
    public final boolean remove(final Object o) {
        synchronized (this) {
            return queue.remove(o);
        }
    }

    @Override
    public final boolean containsAll(final Collection<?> c) {
        synchronized (this) {
            return queue.containsAll(c);
        }
    }

    @Override
    public final boolean addAll(final Collection<? extends E> c) {
        synchronized (this) {
            return queue.addAll(c);
        }
    }

    @Override
    public final boolean removeAll(final Collection<?> c) {
        synchronized (this) {
            return queue.removeAll(c);
        }
    }

    @Override
    public final boolean retainAll(final Collection<?> c) {
        synchronized (this) {
            return queue.retainAll(c);
        }
    }

    @Override
    public final void clear() {
        synchronized (this) {
            queue.clear();
        }
    }

    @Override
    public final E remove() {
        synchronized (this) {
            return queue.remove();
        }
    }

    @Override
    public final E element() {
        synchronized (this) {
            return queue.element();
        }
    }


    @Override
    public final boolean equals(final Object other) {
        if (other == null) {
            return false;
        }

        if (!other.getClass().isInstance(this)) {
            return false;
        }

        SynchronizedBoundedPriorityQueue<E> other_synced =
                (SynchronizedBoundedPriorityQueue<E>) other;

        synchronized (this) {
            return queue.equals(other_synced.queue);
        }
    }

    @Override
    public final int hashCode() {
        synchronized (this) {
            return this.queue.hashCode();
        }
    }

    @Override
    public final String toString() {
        synchronized (this) {
            return queue.toString();
        }
    }
}
