package com.github.algox.model;

import com.github.algox.common.EmbeddingType;
import com.github.algox.common.Pair;
import com.github.algox.common.Searcher;
import com.github.algox.log.LoggerUtils;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EmbeddingModel implements Searcher {
    private final static long ONE_GB = 1024 * 1024 * 1024;
    private final List<String> vocab;
    private final DoubleBuffer vectors;
    private final int layerSize;
    private ImmutableMap<String, Integer> offset;

    public EmbeddingModel(Iterable<String> vocab, double[] vectors, int layerSize) {
        this.vocab = ImmutableList.copyOf(vocab);
        this.vectors = DoubleBuffer.wrap(vectors);
        this.layerSize = layerSize;
    }

    private EmbeddingModel(Iterable<String> vocab, DoubleBuffer vectors, int layerSize) {
        this.layerSize = layerSize;
        this.vectors = vectors;
        this.vocab = ImmutableList.copyOf(vocab);
    }

    public void toBinFile(String path) throws IOException {
        OutputStream out = new FileOutputStream(new File(path));
        final Charset cs = Charset.forName("UTF-8");
        final String header = String.format("%d %d\n", vocab.size(), layerSize);
        out.write(header.getBytes(cs));
        final double[] vector = new double[layerSize];
        final ByteBuffer buffer = ByteBuffer.allocate(4 * layerSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0; i < vocab.size(); ++i) {
            out.write(String.format("%s ", vocab.get(i)).getBytes(cs));
            vectors.position(i * layerSize);
            vectors.get(vector);
            buffer.clear();
            for(int j = 0; j < layerSize; ++j) {
                buffer.putFloat((float)vector[j]);
            }
            out.write(buffer.array());
            out.write('\n');
        }
        out.flush();
    }

    public static EmbeddingModel fromBinFile(File file, EmbeddingType type)
            throws IOException {
        final FileInputStream fis = new FileInputStream(file);
        final FileChannel channel = fis.getChannel();
        MappedByteBuffer buffer =
                channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        0,
                        Math.min(channel.size(), Integer.MAX_VALUE));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int bufferCount = 1;
        // Java's NIO only allows memory-mapping up to 2GB. To work around this problem, we re-map
        // every gigabyte. To calculate offsets correctly, we have to keep track how many gigabytes
        // we've already skipped. That's what this is for.
        StringBuilder sb = new StringBuilder();
        char c = (char) buffer.get();
        while (c != '\n') {
            sb.append(c);
            c = (char) buffer.get();
        }
        String firstLine = sb.toString();
        int index = firstLine.indexOf(' ');
        Preconditions.checkState(index != -1,
                "Expected a space in the first line of file '%s': '%s'",
                file.getAbsolutePath(), firstLine);
        final int vocabSize = Integer.parseInt(firstLine.substring(0, index));
        final int layerSize = Integer.parseInt(firstLine.substring(index + 1));
        List<String> vocabs = new ArrayList<String>(vocabSize);
        DoubleBuffer vectors = ByteBuffer.allocateDirect(vocabSize * layerSize * 8).asDoubleBuffer();
        final float[] floats = new float[layerSize];
        for (int lineno = 0; lineno < vocabSize; lineno++) {
            // read vocab
            sb.setLength(0);
            c = (char) buffer.get();
            while (c != ' ') {
                // ignore newlines in front of words (some binary files have newline,
                // some don't)
                if (c != '\n') {
                    sb.append(c);
                }
                c = (char) buffer.get();
            }
            vocabs.add(sb.toString());
            // read vector
            final FloatBuffer floatBuffer = buffer.asFloatBuffer();
            floatBuffer.get(floats);
            for (int i = 0; i < floats.length; ++i) {
                vectors.put(lineno * layerSize + i, floats[i]);
            }
            buffer.position(buffer.position() + 4 * layerSize);

            // remap file
            if (buffer.position() > ONE_GB) {
                final int newPosition = (int) (buffer.position() - ONE_GB);
                final long size = Math.min(channel.size() - ONE_GB * bufferCount, Integer.MAX_VALUE);
                buffer = channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        ONE_GB * bufferCount,
                        size);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.position(newPosition);
                bufferCount += 1;
            }
        }
        return new EmbeddingModel(vocabs, vectors, layerSize);
    }

    public List<String> getVocab() {
        return vocab;
    }

    public Searcher searcher() {
        for (int i = 0; i < vocab.size(); ++i) {
            double len = 0;
            for(int j = i * layerSize; j < (i + 1) * layerSize; ++j) {
                len += vectors.get(j) * vectors.get(j);
            }
            len = Math.sqrt(len);

            for (int j = i * layerSize; j < (i + 1) * layerSize; ++j) {
                vectors.put(j, vectors.get(j) / len);
            }
        }
        final ImmutableMap.Builder<String, Integer> result = ImmutableMap.builder();
        for (int i = 0; i < vocab.size(); i++) {
            result.put(vocab.get(i), i * layerSize);
        }
        offset = result.build();
        return this;
    }


    @Override
    public boolean contains(String word) {
        return offset.containsKey(word);
    }

    @Override
    public double[] getVector(String word) {
        if (offset.containsKey(word)) {
            final Integer index = offset.get(word);
            final DoubleBuffer vectors = this.vectors.duplicate();
            double[] result = new double[this.layerSize];
            vectors.position(index);
            vectors.get(result);
            return result;
        } else {
            LoggerUtils.error("the model has not the vocab " + word);
            return null;
        }
    }

    @Override
    public List<Matcher> getMatchers(String word, int maxMatches) {
        return getMatchers(getVector(word), maxMatches);
    }

    @Override
    public List<Matcher> getMatchers(double[] vec, int maxMatches) {
        return Matcher.ORDERING.greatestOf(
                Iterables.transform(vocab, new Function<String, Matcher>() {
                    @Nullable
                    @Override
                    public Matcher apply(@Nullable String s) {
                        double[] otherVec = getVectorOrNull(s);
                        double d = calculateDistance(otherVec, vec);
                        return new MatcherImpl(s, d);
                    }
                }), maxMatches
        );
    }

    private double[] getVectorOrNull(final String word) {
        final Integer index = offset.get(word);
        if(index == null) {
            return null;
        }
        final DoubleBuffer vectors = this.vectors.duplicate();
        double[] result = new double[layerSize];
        vectors.position(index);
        vectors.get(result);
        return result;
    }

    @Override
    public SemanticDifference similarity(String w1, String w2) {
        double[] v1 = getVector(w1);
        double[] v2 = getVector(w2);
        final double[] diff = getDifference(v1, v2);
        return new SemanticDifference() {
            @Override
            public List<Matcher> getMatchers(String word, int maxMatches) {
                double[] target = getDifference(getVector(word), diff);
                return EmbeddingModel.this.getMatchers(target, maxMatches);
            }
        };
    }

    @Override
    public double cosineDistance(String w1, String w2) {
        return calculateDistance(getVector(w1), getVector(w2));
    }

    private double[] getDifference(double[] v1, double[] v2) {
        double[] diff = new double[layerSize];
        for (int i = 0; i < layerSize; i++) {
            diff[i] = v1[i] - v2[i];
        }
        return diff;
    }

    private double calculateDistance(double[] otherVec, double[] vec) {
        double d = 0;
        for (int a = 0; a < this.layerSize; a++) {
            d += vec[a] * otherVec[a];
        }
        return d;
    }

    private static class MatcherImpl extends Pair<String, Double> implements Matcher {

        protected MatcherImpl(String first, Double second) {
            super(first, second);
        }

        @Override
        public String match() {
            return first;
        }

        @Override
        public double distance() {
            return second;
        }

        @Override
        public String toString() {
            return String.format("%s [%s]", first, second);
        }
    }
}
