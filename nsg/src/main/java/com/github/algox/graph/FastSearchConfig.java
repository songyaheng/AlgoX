package com.github.algox.graph;

import java.io.Serializable;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 1:55 PM
 * @description:
 */
public class FastSearchConfig implements Serializable {
    /**
     * Fast search: speedup compared to exhaustive search.
     */
    public static final double DEFAULT_SEARCH_SPEEDUP = 4.0;

    /**
     * Fast search: expansion parameter.
     */
    public static final double DEFAULT_SEARCH_EXPANSION = 1.2;

    /**
     * Fast search: number of random jumps per node (to simulate small world
     * graph).
     */
    public static final int DEFAULT_SEARCH_RANDOM_JUMPS = 2;


    /**
     * Get an instance of default search parameters.
     * @return
     */
    public static FastSearchConfig getDefault() {
        FastSearchConfig conf = new FastSearchConfig();
        conf.expansion = DEFAULT_SEARCH_EXPANSION;
        conf.speedup = DEFAULT_SEARCH_SPEEDUP;
        conf.longJumps = DEFAULT_SEARCH_RANDOM_JUMPS;
        conf.restartAtBoundary = true;
        conf.k = 1;
        return conf;
    }

    /**
     * Get a configuration to perform naive search: expansion and long jumps
     * are disabled, algorithm will stop and return at partition boundary.
     * @return
     */
    public static FastSearchConfig getNaive() {
        FastSearchConfig conf = new FastSearchConfig();
        conf.expansion = Double.POSITIVE_INFINITY;
        conf.speedup = DEFAULT_SEARCH_SPEEDUP;
        conf.longJumps = 0;
        conf.restartAtBoundary = false;
        conf.k = 1;
        return conf;
    }

    private int k;
    private double speedup;
    private int longJumps;
    private double expansion;
    private boolean restartAtBoundary = true;

    /**
     *
     * @return
     */
    public final int getK() {
        return k;
    }

    /**
     *
     * @param k
     */
    public final void setK(final int k) {
        this.k = k;
    }

    /**
     *
     * @return
     */
    public final double getSpeedup() {
        return speedup;
    }

    /**
     *
     * @param speedup
     */
    public final void setSpeedup(final double speedup) {
        if (speedup <= 1.0) {
            throw new IllegalArgumentException("Speedup should be > 1.0");
        }

        this.speedup = speedup;
    }

    /**
     *
     * @return
     */
    public final int getLongJumps() {
        return longJumps;
    }

    /**
     *
     * @param longJumps
     */
    public final void setLongJumps(final int longJumps) {
        this.longJumps = longJumps;
    }

    /**
     *
     * @return
     */
    public final double getExpansion() {
        return expansion;
    }

    /**
     *
     * @param expansion
     */
    public final void setExpansion(final double expansion) {
        this.expansion = expansion;
    }

    /**
     *
     * @return
     */
    public final boolean isRestartAtBoundary() {
        return restartAtBoundary;
    }

    /**
     *
     * @param restartAtBoundary
     */
    public final void setRestartAtBoundary(final boolean restartAtBoundary) {
        this.restartAtBoundary = restartAtBoundary;
    }
}
