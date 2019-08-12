package com.github.algox.embedding;

import com.github.algox.common.EmbeddingType;
import com.github.algox.common.Searcher;
import com.github.algox.model.EmbeddingModel;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestModel {
    public static void main(String[] args) throws IOException {
        EmbeddingModel model = EmbeddingModel
                .fromBinFile(new File("/Users/songyaheng/Downloads/model.graph"), EmbeddingType.GRAPH);
        List<Searcher.Matcher> matcherList = model.searcher().getMatchers("u_1", 5);
        for (Searcher.Matcher matcher : matcherList) {
            System.out.println(matcher.match() + " => " + matcher.distance());
        }

    }
}
