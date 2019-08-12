package com.github.algox.graph;

import java.io.Serializable;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 2:18 PM
 * @description:
 */
public class StatisticsContainer implements Serializable {
    private int searchSimilarities;
    private int searchRestarts;
    private int searchCrossPartitionRestarts;

    private int addSimilarities;
    private int removeSimilarities;

    /**
     *
     * @return
     */
    public final int getSearchSimilarities() {
        return searchSimilarities;
    }

    /**
     *
     * @return
     */
    public final int getSearchRestarts() {
        return searchRestarts;
    }

    /**
     *
     * @return
     */
    public final int getSearchCrossPartitionRestarts() {
        return searchCrossPartitionRestarts;
    }

    /**
     *
     * @return
     */
    public final int getAddSimilarities() {
        return addSimilarities;
    }

    /**
     * Return the total number of computed similarities (search + add + remove).
     * @return
     */
    public final int getSimilarities() {
        return searchSimilarities + addSimilarities + removeSimilarities;
    }

    /**
     *
     * @return
     */
    public final int getRemoveSimilarities() {
        return removeSimilarities;
    }

    /**
     *
     */
    public final void incSearchSimilarities() {
        searchSimilarities++;
    }

    /**
     *
     */
    public final void incSearchRestarts() {
        searchRestarts++;
    }

    /**
     *
     */
    public final void incSearchCrossPartitionRestarts() {
        searchCrossPartitionRestarts++;
    }

    /**
     *
     */
    public final void incAddSimilarities() {
        addSimilarities++;
    }

    /**
     *
     */
    public final void incRemoveSimilarities() {
        removeSimilarities++;
    }

    /**
     *
     * @param value
     */
    public final void incSearchSimilarities(final int value) {
        searchSimilarities += value;
    }

    /**
     *
     * @param value
     */
    public final void incSearchRestarts(final int value) {
        searchRestarts += value;
    }

    /**
     *
     * @param value
     */
    public final void incSearchCrossPartitionRestarts(final int value) {
        searchCrossPartitionRestarts += value;
    }

    /**
     *
     * @param value
     */
    public final void incAddSimilarities(final int value) {
        addSimilarities += value;
    }

    /**
     *
     * @param value
     */
    public final void incRemoveSimilarities(final int value) {
        removeSimilarities += value;
    }

    @Override
    public final String toString() {
        return String.format(
                "Search similarities: %d\n"
                        + "Search restarts: %d\n"
                        + "Search cross-partition restarts: %d\n"
                        + "Add similarities: %d\n"
                        + "Remove similarities: %d\n",
                searchSimilarities,
                searchRestarts,
                searchCrossPartitionRestarts,
                addSimilarities,
                removeSimilarities);
    }
}
