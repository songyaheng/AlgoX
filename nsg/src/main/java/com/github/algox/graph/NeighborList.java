package com.github.algox.graph;

import com.github.algox.utils.SynchronizedBoundedPriorityQueue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 1:12 PM
 * @description:
 */
public class NeighborList extends SynchronizedBoundedPriorityQueue<Neighbor> implements Serializable {
    /**
     * Copy constructor.
     *
     * @param origin
     */
    public NeighborList(final NeighborList origin) {
        super(origin.size());
        this.addAll(origin);
    }

    /**
     * Create a new neighborlist of given size.
     * @param size size of the neighborlist
     */
    public NeighborList(final int size) {
        super(size);
    }

    /**
     * Count the nodes (based on node.id) that are present in both
     * neighborlists.
     *
     * @param otherNl
     * @return
     */
    public final int countCommonNodes(final NeighborList otherNl) {
        int count = 0;
        for (Neighbor n : this) {
            if (otherNl.contains(n)) {
                count++;
            }
        }
        return count;
    }

    // double has 15 significant digits
    private static final double EPSILON = 1E-12;

    /**
     * Count the number of equivalent neighbors (using similarities).
     * @param other
     * @return
     */
    public final int countCommons(final NeighborList other) {
        // Make a copy of both neighborlists
        PriorityQueue<Neighbor> copyThis = new PriorityQueue<Neighbor>(this);
        PriorityQueue<Neighbor> copyOther = new PriorityQueue<Neighbor>(other);

        int count = 0;
        Neighbor thisNeighbor = copyThis.poll();
        Neighbor otherNeighbor = copyOther.poll();

        while (true) {
            if (thisNeighbor == null || otherNeighbor == null) {
                // We reached the end of at least one neighborlist
                break;
            }
            double delta = thisNeighbor.getSimilarity() - otherNeighbor.getSimilarity();
            if (delta < EPSILON && delta > -EPSILON) {
                count++;
                thisNeighbor = copyThis.poll();
                otherNeighbor = copyOther.poll();

            } else if (thisNeighbor.getSimilarity()
                    > otherNeighbor.getSimilarity()) {
                otherNeighbor = copyOther.poll();

            } else {
                thisNeighbor = copyThis.poll();
            }
        }
        return count;
    }

    /**
     * Returns true if this neighborlist contains a neighbor corresponding to
     * this node.
     * @param node
     * @return
     */
    public final <T> boolean containsNode(final T node) {
        for (Neighbor n : this) {
            if (n.getNode().equals(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove from the neighborlist the neighbor corresponding to this node.
     * @param node
     * @return true if a neighbor was effectively removed from the list.
     */
    public final <T> boolean removeNode(final T node) {
        for (Neighbor n : this) {
            if (n.getNode().equals(node)) {
                this.remove(n);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all neighbors with similarity inferior to threshold.
     * @param threshold
     */
    public final void prune(final double threshold) {
        removeIf(neighbor -> neighbor.getSimilarity() < threshold);
    }
}
