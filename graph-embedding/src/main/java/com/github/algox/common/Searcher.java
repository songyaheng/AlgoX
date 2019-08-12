package com.github.algox.common;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public interface Searcher {
    /**
     * 模型中是否包含该词的向量
     * @param word
     * @return
     */
    boolean contains(String word);

    /**
     * 返回词的向量
     * @param word
     * @return
     */
    double[] getVector(String word);

    List<Matcher> getMatchers(String word, int maxMatches);

    List<Matcher> getMatchers(final double[] vec, int maxMatches);


    SemanticDifference similarity(String w1, String w2);

    double cosineDistance(String w1, String w2);


    public interface SemanticDifference {
        List<Matcher> getMatchers(String word, int maxMatches);
    }

    public interface Matcher {
        String match();
        double distance();
        Ordering<Matcher> ORDERING = Ordering.natural()
                .onResultOf(new Function<Matcher, Double>() {
                    @Nullable
                    @Override
                    public Double apply(@Nullable Matcher matcher) {
                        return matcher.distance();
                    }
                });
        Function<Matcher, String> TO_WORD = new Function<Matcher, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Matcher matcher) {
                return matcher.match();
            }
        };
    }
}
