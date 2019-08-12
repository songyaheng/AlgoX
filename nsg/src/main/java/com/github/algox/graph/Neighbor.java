package com.github.algox.graph;

import java.io.Serializable;
import java.security.InvalidParameterException;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 1:13 PM
 * @description:
 */
public class Neighbor<T> implements Comparable, Serializable {
    private final T node;
    private final double similarity;

    public Neighbor(final T node, final double similarity) {
        this.node = node;
        this.similarity = similarity;
    }

    public final T getNode() {
        return node;
    }

    public final double getSimilarity() {
        return similarity;
    }

    @Override
    public final String toString() {
        return "(" + node.toString() + "," + similarity + ")";
    }

    /**
     * A neighbor has no reference to the origin node, hence only neighbors
     * from the same origin can be compared.
     * @param other
     * @return
     */
    @Override
    public final boolean equals(final Object other) {
        if (!other.getClass().getName().equals(this.getClass().getName())) {
            return false;
        }

        Neighbor other_neighbor = (Neighbor) other;
        return this.node.equals(other_neighbor.node);
    }

    @Override
    public final int hashCode() {
        return this.node.hashCode();
    }

    /**
     * This > other if this.similarity > other.similarity.
     * @param other
     * @return
     */
    @Override
    public final int compareTo(final Object other) {
        if (other == null) {
            return 1;
        }

        if (!other.getClass().isInstance(this)) {
            throw new InvalidParameterException();
        }

        if (((Neighbor) other).node.equals(this.node)) {
            return 0;
        }

        if (this.similarity == ((Neighbor) other).similarity) {
            return 0;
        }

        if (this.similarity > ((Neighbor) other).similarity) {
            return 1;
        }

        return -1;
    }
}
