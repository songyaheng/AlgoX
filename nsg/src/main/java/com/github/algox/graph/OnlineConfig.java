package com.github.algox.graph;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 2:21 PM
 * @description:
 */
public class OnlineConfig extends FastSearchConfig {
    /**
     * Depth for updating edges when adding or removing nodes.
     */
    public static final int DEFAULT_UPDATE_DEPTH = 3;

    private int updateDepth = DEFAULT_UPDATE_DEPTH;

    /**
     *
     * @return
     */
    public final int getUpdateDepth() {
        return updateDepth;
    }

    /**
     *
     * @param updateDepth
     */
    public final void setUpdateDepth(final int updateDepth) {
        this.updateDepth = updateDepth;
    }

    /**
     *
     * @return
     */
    public static OnlineConfig getDefault() {
        OnlineConfig conf = new OnlineConfig();
        conf.setExpansion(DEFAULT_SEARCH_EXPANSION);
        conf.setLongJumps(DEFAULT_SEARCH_RANDOM_JUMPS);
        conf.setSpeedup(DEFAULT_SEARCH_SPEEDUP);
        return conf;
    }
}
