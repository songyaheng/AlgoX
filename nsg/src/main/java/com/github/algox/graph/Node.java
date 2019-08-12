package com.github.algox.graph;

import java.io.Serializable;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 12:50 PM
 * @description:
 */
public class Node<T> implements Serializable {
    public T value;
    public long id;
    public int partition;

    /**
     * Initialize a node with id and partition = 0.
     * @param value
     */
    public Node(final T value) {
        this.value = value;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 29 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node<?> other = (Node<?>) obj;
        return this.id == other.id;
    }

    @Override
    public final String toString() {
        return "Node{" + "value=" + value + ", id=" + id
                + ", partition=" + partition + '}';
    }
}
