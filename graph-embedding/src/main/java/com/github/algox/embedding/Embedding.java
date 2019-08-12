package com.github.algox.embedding;

import com.github.algox.config.NNConfig;
import com.github.algox.huffman.HuffmanNode;
import com.github.algox.huffman.HuffmanTree;
import com.github.algox.log.LoggerUtils;
import com.github.algox.model.EmbeddingModel;
import com.github.algox.model.ModelType;
import com.github.algox.utils.CommonUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.common.primitives.Doubles;

import java.util.List;
import java.util.Map;

public abstract class Embedding {
    private int minFrequency;
    private int numThread;
    private int iterations;
    private ModelType type;
    private int layerSize;
    private int windowSize;
    private int negativeSamples;
    private boolean useHierarchicalSoftmax;
    private double initialLearningRate;
    private double downSampleRate;

    protected Embedding() {
        this.minFrequency = 3;
        this.numThread = 1;
        this.iterations = 25;
        this.type = ModelType.SKIP_GRAM;
        this.layerSize = 64;
        this.windowSize = 5;
        this.negativeSamples = 0;
        this.useHierarchicalSoftmax = true;
        this.initialLearningRate = 0.025;
        this.downSampleRate = 1e-4;
    }

    public Embedding withMinFrequency(int minFrequency) {
        Preconditions.checkArgument(numThread > 0, "Value must be positive");
        this.minFrequency = minFrequency;
        return this;
    }
    public Embedding withNumThread(int numThread) {
        Preconditions.checkArgument(numThread > 0, "Value must be positive");
        this.numThread = numThread;
        return this;
    }

    public Embedding withIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    public Embedding withType(ModelType type) {
        this.type = Preconditions.checkNotNull(type);
        return this;
    }

    public Embedding withLayerSize(int layerSize) {
        Preconditions.checkArgument(layerSize > 0, "Value must be positive");
        this.layerSize = layerSize;
        return this;
    }

    public Embedding withWindowSize(int windowSize) {
        Preconditions.checkArgument(windowSize > 0, "Value must be positive");
        this.windowSize = windowSize;
        return this;
    }

    public Embedding withNegativeSamples(int negativeSamples) {
        Preconditions.checkArgument(negativeSamples >= 0, "Value must be non-negative");
        this.negativeSamples = negativeSamples;
        return this;
    }

    public Embedding withHierarchicalSoftmax() {
        this.useHierarchicalSoftmax = true;
        return this;
    }

    public Embedding withInitialLearningRate(double initialLearningRate) {
        this.initialLearningRate = initialLearningRate;
        return this;
    }

    public Embedding withDownSampleRate(double downSampleRate) {
        this.downSampleRate = downSampleRate;
        return this;
    }


    private Multiset<String> count(Iterable<String> tokens) {
        Multiset<String> counts = HashMultiset.create();
        for (String token : tokens) {
            counts.add(token);
        }
        return counts;
    }

    private ImmutableMultiset<String> filterAndSort(final Multiset<String> counts) {
        return Multisets.copyHighestCountFirst(
                ImmutableSortedMultiset.copyOf(
                        Multisets.filter(
                                counts,
                                new Predicate<String>() {
                                    @Override
                                    public boolean apply(String s) {
                                        return counts.count(s) >= minFrequency;
                                    }
                                }
                        )
                )
        );
    }

    protected abstract Iterable<List<String>> sentences();

    public EmbeddingModel train() {
        long time = System.currentTimeMillis();
        LoggerUtils.info("preparing the data ...");
        Iterable<List<String>> sentences = sentences();
        LoggerUtils.info("start to train model at [%s] \n building the vocab ...", CommonUtils.getNow());
        Multiset<String> counts = count(Iterables.concat(sentences));
        ImmutableMultiset<String> vocab = filterAndSort(counts);
        LoggerUtils.info("the vocab size is %d \n building the huffman tree ...", vocab.size());
        Map<String, HuffmanNode> huffmanNodes = new HuffmanTree(vocab).encode();
        LoggerUtils.info("building huffman tree success and start train the model ...");
        AbstractTrainer.NeuralNetworkModel model = type.createTrainer(new NNConfig(
                minFrequency,
                numThread,
                iterations,
                layerSize,
                windowSize,
                negativeSamples,
                useHierarchicalSoftmax,
                initialLearningRate,
                downSampleRate),
                vocab, huffmanNodes).train(sentences);
        LoggerUtils.info("train data success at %s used time %ds", CommonUtils.getNow(), (System.currentTimeMillis() - time) / 1000);
        return new EmbeddingModel(vocab.elementSet(), Doubles.concat(model.vectors()), layerSize);
    }

}
