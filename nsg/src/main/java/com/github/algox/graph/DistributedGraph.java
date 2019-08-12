package com.github.algox.graph;

import com.github.algox.commons.SimilarityInterface;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 1:04 PM
 * @description:
 */
public class DistributedGraph {
    /**
     * Wrap the nodes with a node class (which decorates with an id
     * and partition).
     * @param <T>
     * @param nodes
     * @return
     */
    public static final <T> JavaRDD<Node<T>> wrapNodes(
            final JavaRDD<T> nodes) {
        return nodes.zipWithUniqueId().map(new WrapNodeFunction());
    }

    /**
     *
     * @param <T>
     * @param graph1
     * @param graph2
     * @return
     */
    public static final <T> long countCommonEdges(
            final JavaPairRDD<Node<T>, NeighborList> graph1,
            final JavaPairRDD<Node<T>, NeighborList> graph2) {

        return (long) graph1
                .union(graph2)
                .groupByKey()
                .map(new CountFunction())
                .reduce(new SumFunction());
    }

    /**
     * Convert a PairRDD of (Node, NeighborList) to a RDD of Graph.
     * @param <T>
     * @param graph
     * @param similarity
     * @return
     */
    public static final <T> JavaRDD<Graph<Node<T>>> toGraph(
            final JavaPairRDD<Node<T>, NeighborList> graph,
            final SimilarityInterface<T> similarity) {

        return graph.mapPartitions(
                new NeighborListToGraph(similarity), true);
    }

    /**
     *
     * @param <T>
     * @param graph
     * @return
     */
    public static final <T> long size(
            final JavaRDD<Graph<T>> graph) {

        long total = 0;
        JavaRDD<Long> sizes = graph.map(new GraphSizeFunction());
        for (Long size : sizes.collect()) {
            total += size;
        }
        return total;
    }
}

/**
 * Return the size of the graph.
 *
 * @author tibo
 * @param <T>
 */
class GraphSizeFunction<T> implements Function<Graph<T>, Long> {

    @Override
    public Long call(final Graph<T> graph) {
        return (long) graph.size();
    }
}

/**
 * Wrap values of type T with a Node class (which holds a unique id and a
 * partition id).
 *
 * @author tibo
 * @param <T>
 */
class WrapNodeFunction<T> implements Function<Tuple2<T, Long>, Node<T>> {

    @Override
    public Node<T> call(
            final Tuple2<T, Long> value) {

        Node<T> node = new Node<>(value._1);
        node.id = value._2;
        return node;
    }
}

/**
 * Used to convert a PairRDD Node,NeighborList to a RDD of Graph.
 * @author Thibault Debatty
 * @param <T>
 */
class NeighborListToGraph<T>
        implements FlatMapFunction<
                Iterator<Tuple2<Node<T>, NeighborList>>, Graph<Node<T>>> {

    private final SimilarityInterface<T> similarity;

    NeighborListToGraph(final SimilarityInterface<T> similarity) {

        this.similarity = similarity;
    }

    @Override
    public Iterator<Graph<Node<T>>> call(
            final Iterator<Tuple2<Node<T>, NeighborList>> iterator) {

        Graph<Node<T>> graph = new Graph<>();
        while (iterator.hasNext()) {
            Tuple2<Node<T>, NeighborList> next = iterator.next();
            graph.put(next._1, next._2);
        }
        graph.setSimilarity(new NodeSimilarityAdapter<>(similarity));
        graph.setK(graph.getNeighbors(graph.getNodes().iterator().next()).size());
        ArrayList<Graph<Node<T>>> list = new ArrayList<>(1);
        list.add(graph);
        return list.iterator();
    }
}

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
class CountFunction<T>
        implements Function<Tuple2<T, Iterable<NeighborList>>, Long> {

    /**
     *
     * @param tuples
     * @return
     */
    @Override
    public Long call(final Tuple2<T, Iterable<NeighborList>> tuples) {
        Iterator<NeighborList> iterator = tuples._2.iterator();
        NeighborList nl1 = iterator.next();
        NeighborList nl2 = iterator.next();
        return new Long(nl1.countCommons(nl2));
    }

}

/**
 *
 * @author Thibault Debatty
 */
class SumFunction
        implements Function2<Long, Long, Long> {
    /**
     *
     * @param arg0
     * @param arg1
     * @return
     * @throws Exception
     */
    @Override
    public Long call(final Long arg0, final Long arg1) {
        return arg0 + arg1;
    }
}
