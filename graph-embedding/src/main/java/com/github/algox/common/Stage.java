package com.github.algox.common;

public enum Stage {
    /**
     * 获取词表
     */
    ACQUIRE_VOCAB,
    /**
     * 过滤并对词表排序
     */
    FILTER_SORT_VOCAB,
    /**
     * 霍夫曼编码
     */
    CREATE_HUFFMAN_ENCODING,
    /**
     * 训练网络
     */
    TRAIN_NEURAL_NETWORK
}
