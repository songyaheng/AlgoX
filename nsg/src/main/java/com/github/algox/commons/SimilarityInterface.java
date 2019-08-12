package com.github.algox.commons;

import java.io.Serializable;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 1:18 PM
 * @description:
 */
public interface SimilarityInterface<T> extends Serializable {
    /**
     * Compute the similarity between two nodes.
     * @param node1
     * @param node2
     * @return
     */
    double similarity(final T node1, final T node2);
}
