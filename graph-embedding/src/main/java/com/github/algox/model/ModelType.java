package com.github.algox.model;

import com.github.algox.config.NNConfig;
import com.github.algox.embedding.AbstractTrainer;
import com.github.algox.huffman.HuffmanNode;
import com.google.common.collect.Multiset;

import java.util.Map;

public enum  ModelType {
    CBOW {
        @Override
        public double getDefaultInitialLearningRate() {
            return 0.05;
        }

        @Override
        public AbstractTrainer createTrainer(NNConfig config, Multiset<String> counts, Map<String, HuffmanNode> huffmanNodes) {
            return new CBOWModel(config, counts, huffmanNodes);
        }
    },
    SKIP_GRAM {
        @Override
        public double getDefaultInitialLearningRate() {
            return 0.025;
        }

        @Override
        public AbstractTrainer createTrainer(NNConfig config, Multiset<String> counts, Map<String, HuffmanNode> huffmanNodes) {
            return new SkipGramModel(config, counts, huffmanNodes);
        }
    };

    public abstract double getDefaultInitialLearningRate();

    public abstract AbstractTrainer createTrainer(NNConfig config, Multiset<String> counts, Map<String, HuffmanNode> huffmanNodes);

}
