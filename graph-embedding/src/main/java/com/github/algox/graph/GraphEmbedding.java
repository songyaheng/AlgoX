package com.github.algox.graph;


import com.github.algox.common.Tuple;
import com.github.algox.embedding.Embedding;
import com.github.algox.exception.UnGetValueException;
import com.github.algox.log.LoggerUtils;
import com.github.algox.utils.CommonUtils;
import com.google.common.base.Preconditions;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import java.util.*;

public class GraphEmbedding extends Embedding{

    private ValueGraph<String, Double> graph;
    private double p;
    private double q;
    private int numWalks;
    private int walkLength;
    private Map<String, Tuple<int[], double[]>> aliasNodes;
    private Map<String, Tuple<int[], double[]>> aliasEdges;

    private GraphEmbedding(ValueGraph<String, Double> graph) {
        super();
        this.graph = graph;
        this.p = 1.0;
        this.q = 1.0;
        this.numWalks = 10;
        this.walkLength = 40;
        this.aliasNodes = new HashMap<>(graph.nodes().size());
        this.aliasEdges = new HashMap<>(graph.edges().size());
    }

    public static GraphEmbedding from(ValueGraph<String, Double> graph) {
        LoggerUtils.info("the input graph size is %d", graph.nodes().size());
        return new GraphEmbedding(graph);
    }

    public GraphEmbedding withQ(double q) {
        Preconditions.checkArgument(q > 0, "Value must be positive");
        this.q = q;
        return this;
    }

    public GraphEmbedding withP(double p) {
        Preconditions.checkArgument(p > 0, "Value must be positive");
        this.p = p;
        return this;
    }

    public GraphEmbedding withNumWalks(int numWalks) {
        Preconditions.checkArgument(numWalks > 0, "Value must be positive");
        this.numWalks = numWalks;
        return this;
    }

    public GraphEmbedding withWalkLenght(int walkLenght) {
        Preconditions.checkArgument(walkLenght > 0, "Value must be positive");
        this.walkLength = walkLenght;
        return this;
    }

    private Iterable<List<String>> simulateWalks() {
        processTransitionProbs();
        List<List<String>> walks = new ArrayList<>(numWalks * graph.nodes().size());
        List<String> nodes = new ArrayList<>(graph.nodes());
        long count = 0;
        long total = nodes.size() * numWalks;
        for (int i = 0; i < numWalks; i++) {
            Collections.shuffle(nodes);
            for (String node : nodes) {
                walks.add(walk(walkLength, node));
                count ++;
            }
            LoggerUtils.info("the walk is running at step %d complete %.2f%%", count, (double) count / total * 100);
        }
        return walks;
    }

    private List<String> walk(int walkLenght, String node) {
        List<String> walk = new ArrayList<>(walkLenght);
        walk.add(node);
        while (walk.size() < walkLenght) {
            String cur = walk.get(walk.size() -1);
            List<String> nbs = new ArrayList<>(graph.successors(cur));
            if (nbs.size() > 0) {
                Collections.sort(nbs);
                if (walk.size() == 1) {
                    walk.add(nbs.get(aliasDraw(aliasNodes.get(cur))));
                } else {
                    String pre = walk.get(walk.size() - 2);
                    String k = getKey(pre, cur);
                    if (aliasEdges.containsKey(k)) {
                        walk.add(nbs.get(aliasDraw(aliasEdges.get(k))));
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        return walk;
    }

    private int aliasDraw(Tuple<int[], double[]> tp) {
        int k = tp._1.length - 1;
        int kk = (int) Math.floor(Math.random() * k);
        if (Math.random() < tp._2[kk]) {
            return kk;
        } else {
            return tp._1[kk];
        }
    }

    private void processTransitionProbs() {
        for (String node : graph.nodes()) {
            List<String> nbs = new ArrayList<>(graph.successors(node));
            Collections.sort(nbs);
            double[] unnormalizedProbs = new double[nbs.size()];
            int i = 0;
            for (String nb : nbs) {
                unnormalizedProbs[i] = graph.edgeValue(node, nb).orElseThrow(UnGetValueException::new);
                i ++;
            }
            double[] normalizedProbs = CommonUtils.probs(unnormalizedProbs);
            aliasNodes.put(node, aliasSetup(normalizedProbs));
        }
        if (graph.isDirected()) {
            for (EndpointPair<String> edge : graph.edges()) {
                String k = getKey(edge.nodeU(), edge.nodeV());
                Tuple<int[], double[]> tp = getAliasEdge(edge.nodeU(), edge.nodeV());
                if (tp != null) {
                    aliasEdges.put(k, tp);
                }
            }
        } else {
            for (EndpointPair<String> edge : graph.edges()) {
                String k = getKey(edge.nodeU(), edge.nodeV());
                Tuple<int[], double[]> tp = getAliasEdge(edge.nodeU(), edge.nodeV());
                if (tp != null) {
                    aliasEdges.put(k, tp);
                }
                k = getKey(edge.nodeV(), edge.nodeU());
                tp = getAliasEdge(edge.nodeV(), edge.nodeU());
                if (tp != null) {
                    aliasEdges.put(k, tp);
                }
            }
        }
    }

    private Tuple<int[], double[]> getAliasEdge(String src, String dst) {
        List<String> dstNbs = new ArrayList<>(graph.successors(dst));
        if (dstNbs.size() == 0) {
            return null;
        }
        Collections.sort(dstNbs);
        double[] unnormalizedProbs = new double[dstNbs.size()];
        int i = 0;
        for (String dstNb : dstNbs) {
            if (dstNb.equals(src)) {
                unnormalizedProbs[i] = graph.edgeValue(dst, dstNb)
                        .orElseThrow(UnGetValueException::new) / p;
            } else if (graph.hasEdgeConnecting(dstNb, src)) {
                unnormalizedProbs[i] = graph.edgeValue(dst, dstNb)
                        .orElseThrow(UnGetValueException::new);
            } else {
                unnormalizedProbs[i] = graph.edgeValue(dst, dstNb)
                        .orElseThrow(UnGetValueException::new) / q;
            }
            i ++;
        }
        double[] normalizedProbs = CommonUtils.probs(unnormalizedProbs);
        return aliasSetup(normalizedProbs);
    }


    private String getKey(String src, String dst) {
        return src + "_" + dst;
    }

    private Tuple<int[], double[]> aliasSetup(double[] normalizedProbs) {
        int k = normalizedProbs.length;
        double[] q = new double[k];
        int[] J = new int[k];
        ArrayDeque<Integer> smaller = new ArrayDeque<>(k);
        ArrayDeque<Integer> larger = new ArrayDeque<>(k);
        int i = 0;
        for (double prob : normalizedProbs) {
            q[i] = k * prob;
            if (q[i] < 1.0) {
                smaller.add(i);
            } else {
                larger.add(i);
            }
            i ++;
        }
        while (smaller.size() > 0 && larger.size() > 0) {
            int small = smaller.pop();
            int large = larger.pop();
            J[small] = large;
            q[large] = q[large] + q[small] - 1.0;
            if (q[large] < 1.0) {
                smaller.add(large);
            } else {
                larger.add(large);
            }
        }
        return new Tuple<>(J, q);
    }

    @Override
    public Iterable<List<String>> sentences() {
        return simulateWalks();
    }
}
