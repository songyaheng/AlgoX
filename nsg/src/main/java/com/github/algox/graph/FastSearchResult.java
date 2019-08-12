package com.github.algox.graph;

import java.io.Serializable;
import java.util.List;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 1:52 PM
 * @description:
 */
public class FastSearchResult<T> implements Serializable {
    private int similarities;
    private int restarts;
    private int boundaryRestarts;
    private final NeighborList neighbors;
    private T boundaryNode;

    /**
     * Initialize a result for a NN list of size k.
     *
     * @param k
     */
    public FastSearchResult(final int k) {
        this.neighbors = new NeighborList(k);
    }

    /**
     * Number of computed similarities.
     * @return
     */
    public final int getSimilarities() {
        return similarities;
    }

    /**
     * Number of restarts because we reached a local maximum.
     * @return
     */
    public final int getRestarts() {
        return restarts;
    }

    /**
     * Number of restarts because we reached the boundary of the partition.
     * @return
     */
    public final int getBoundaryRestarts() {
        return boundaryRestarts;
    }

    /**
     * Get the k most similar neighbors that we found.
     * @return
     */
    public final NeighborList getNeighbors() {
        return neighbors;
    }

    final void incSimilarities() {
        similarities++;
    }

    final void incRestarts() {
        restarts++;
    }

    final void incBoundaryRestarts() {
        boundaryRestarts++;
    }

    /**
     * If we stopped searching because of a boundary, this will contain
     * the boundary node.
     * @return
     */
    public final T getBoundaryNode() {
        return boundaryNode;
    }

    /**
     *
     * @param boundaryNode
     */
    final void setBoundaryNode(final T boundaryNode) {
        this.boundaryNode = boundaryNode;
    }

    /**
     * Append the result contained in the other result to this object.
     *
     * @param other
     */
    public final void add(final FastSearchResult<T> other) {
        this.similarities += other.similarities;
        this.boundaryRestarts += other.boundaryRestarts;
        this.neighbors.addAll(other.neighbors);
        this.restarts += other.restarts;
    }

    /**
     * Append all other results to this object.
     * @param others
     */
    public final void addAll(final List<FastSearchResult<T>> others) {
        for (FastSearchResult<T> other : others) {
            this.add(other);
        }
    }

    @Override
    public final String toString() {
        return "FastSearchResult{" + "similarities=" + similarities
                + ", restarts=" + restarts + ", boundaryRestarts="
                + boundaryRestarts + ", neighbors=" + neighbors
                + ", boundaryRode=" + boundaryNode + '}';
    }
}
