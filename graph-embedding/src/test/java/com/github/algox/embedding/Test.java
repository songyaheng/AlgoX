package com.github.algox.embedding;

import com.github.algox.graph.GraphEmbedding;
import com.github.algox.model.EmbeddingModel;
import com.github.algox.model.ModelType;
import com.github.algox.utils.CommonUtils;
import com.google.common.base.Splitter;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import java.io.IOException;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException {
        EmbeddingModel model = GraphEmbedding.from(graph())
                .withP(1.0)
                .withQ(1.0)
                .withNumWalks(2)
                .withWalkLenght(10)
                .withDownSampleRate(0.025)
                .withHierarchicalSoftmax()
                .withInitialLearningRate(0.02)
                .withIterations(20)
                .withLayerSize(64)
                .withMinFrequency(6)
                .withNegativeSamples(3)
                .withNumThread(3)
                .withType(ModelType.SKIP_GRAM)
                .withWindowSize(3)
                .train();
        model.toBinFile("/Users/songyaheng/Downloads/model.graph");
    }

    private static ValueGraph<String, Double> graph() throws IOException {
        List<String> data = CommonUtils.loadDataFromResource(Test.class, "links.csv");
        MutableValueGraph<String, Double> graph = ValueGraphBuilder.undirected().build();
        for (String line : data) {
            List<String> list = Splitter.on(",")
                    .trimResults()
                    .splitToList(line);
            graph.addNode("u_" + list.get(0));
            graph.addNode("m_" + list.get(1));
            graph.putEdgeValue("u_" + list.get(0), "m_" + list.get(1), Double.valueOf(list.get(2)));
        }
        return graph;
    }
}
