package com.github.algox.word;

import com.github.algox.embedding.Embedding;

import java.util.List;

public class WordEmbedding extends Embedding {
    private Iterable<List<String>> sentences;

    private WordEmbedding(Iterable<List<String>> sentences) {
        this.sentences = sentences;
    }

    public static Embedding from(Iterable<List<String>> sentences) {
        return new WordEmbedding(sentences);
    }

    @Override
    protected Iterable<List<String>> sentences() {
        return sentences;
    }
}
