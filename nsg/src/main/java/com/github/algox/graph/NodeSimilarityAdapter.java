package com.github.algox.graph;

import com.github.algox.commons.SimilarityInterface;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 2:33 PM
 * @description:
 */
public class NodeSimilarityAdapter<T> implements SimilarityInterface<Node<T>> {
    private final SimilarityInterface<T> innerSimilarity;

    /**
     *
     * @param innerSimilarity
     */
    public NodeSimilarityAdapter(
            final SimilarityInterface<T> innerSimilarity) {
        this.innerSimilarity = innerSimilarity;
    }

    /**
     *
     * @param node1
     * @param node2
     * @return
     */
    @Override
    public final double similarity(final Node<T> node1, final Node<T> node2) {
        return innerSimilarity.similarity(node1.value, node2.value);
    }
}
