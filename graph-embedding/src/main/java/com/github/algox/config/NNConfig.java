package com.github.algox.config;

public class NNConfig {
    public int minFrequency;
    public int numThread;
    public int iterations;
    public int layerSize;
    public int windowSize;
    public int negativeSamples;
    public boolean useHierarchicalSoftmax;
    public double initialLearningRate;
    public double downSampleRate;

    public NNConfig(int minFrequency, int numThread,
                    int iterations, int layerSize, int windowSize,
                    int negativeSamples, boolean useHierarchicalSoftmax,
                    double initialLearningRate, double downSampleRate) {
        this.minFrequency = minFrequency;
        this.numThread = numThread;
        this.iterations = iterations;
        this.layerSize = layerSize;
        this.windowSize = windowSize;
        this.negativeSamples = negativeSamples;
        this.useHierarchicalSoftmax = useHierarchicalSoftmax;
        this.initialLearningRate = initialLearningRate;
        this.downSampleRate = downSampleRate;
    }
}
