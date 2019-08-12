package com.github.algox.graph;

import com.github.algox.commons.SimilarityInterface;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author: yaheng.song
 * @date: 2019/4/11 1:06 PM
 * @description:
 */
public class Graph<T> implements Serializable {
    /**
     * Number of edges per node.
     */
    public static final int DEFAULT_K = 10;

    private final HashMap<T, NeighborList> map;
    private SimilarityInterface<T> similarity;
    private int k = DEFAULT_K;

    // These fields are not serialized...
    // drawback: must be very carefull when using/modifying
    private final transient Random rand = new Random();
    // The nodes arraylist is a cache for map.keys()
    // used for example to get a single random node
    private transient ArrayList<T> nodes;

    /**
     * Copy constructor.
     *
     * @param origin
     */
    public Graph(final Graph<T> origin) {
        this.k = origin.k;
        this.similarity = origin.similarity;
        this.map = new HashMap<T, NeighborList>(origin.size());
        for (T node : origin.getNodes()) {
            this.map.put(node, new NeighborList(origin.getNeighbors(node)));
        }
    }

    /**
     * Initialize an empty graph, and set k (number of edges per node).
     * Default k is 10.
     * @param k
     */
    public Graph(final int k) {
        this.k = k;
        this.map = new HashMap<T, NeighborList>();
    }

    /**
     * Initialize an empty graph with k = 10.
     */
    public Graph() {
        this.map = new HashMap<T, NeighborList>();
    }

    /**
     * Get the similarity measure.
     * @return
     */
    public final SimilarityInterface<T> getSimilarity() {
        return similarity;
    }

    /**
     * Set the similarity measure used to build or search the graph.
     * @param similarity
     */
    public final void setSimilarity(final SimilarityInterface<T> similarity) {
        this.similarity = similarity;
    }

    /**
     * Get k (the number of edges per node).
     * @return
     */
    public final int getK() {
        return k;
    }

    /**
     * Set k (the number of edges per node).
     * The existing graph will not be modified.
     * @param k
     */
    public final void setK(final int k) {
        this.k = k;
    }

    /**
     * Get the neighborlist of this node.
     *
     * @param node
     * @return the neighborlist of this node
     */
    public final NeighborList getNeighbors(final T node) {
        return map.get(node);
    }

    /**
     * Get the first node in the graph.
     *
     * @return The first node in the graph
     * @throws NoSuchElementException if the graph is empty...
     */
    public final T first() throws NoSuchElementException {
        return this.getNodes().iterator().next();
    }

    /**
     * Return a random node from the graph.
     * @return
     */
    public final T getRandomNode() {


        return getNodes().get(rand.nextInt(getNodes().size()));
    }

    /**
     * Remove from the graph all edges with a similarity lower than threshold.
     *
     * @param threshold
     */
    public final void prune(final double threshold) {

        for (NeighborList nl : map.values()) {
            nl.prune(threshold);
        }
    }

    /**
     * Split the graph in connected components (usually you will first prune the
     * graph to remove "weak" edges).
     *
     * @return
     */
    public final ArrayList<Graph<T>> connectedComponents() {

        ArrayList<Graph<T>> subgraphs = new ArrayList<Graph<T>>();
        LinkedList<T> nodes_to_process = new LinkedList<T>(map.keySet());
        while (!nodes_to_process.isEmpty()) {
            T n = nodes_to_process.peek();
            if (n == null) {
                continue;
            }
            Graph<T> subgraph = new Graph<T>();
            subgraphs.add(subgraph);
            addAndFollow(subgraph, n, nodes_to_process);
        }
        return subgraphs;
    }

    private void addAndFollow(
            final Graph<T> subgraph,
            final T node,
            final LinkedList<T> nodesToProcess) {

        nodesToProcess.remove(node);

        NeighborList neighborlist = this.getNeighbors(node);
        subgraph.put(node, neighborlist);

        if (neighborlist == null) {
            return;
        }

        for (Neighbor<T> neighbor : this.getNeighbors(node)) {
            if (!subgraph.containsKey(neighbor.getNode())) {
                addAndFollow(subgraph, neighbor.getNode(), nodesToProcess);
            }
        }
    }

    /**
     * Computes the strongly connected sub-graphs (where every node is reachable
     * from every other node) using Tarjan's algorithm, which has computation
     * cost O(n).
     *
     * @return
     */
    public final ArrayList<Graph<T>> stronglyConnectedComponents() {

        Stack<NodeParent> explored_nodes = new Stack<NodeParent>();
        Index index = new Index();
        HashMap<T, NodeProperty> bookkeeping =
                new HashMap<T, NodeProperty>(map.size());

        ArrayList<Graph<T>> connected_components = new ArrayList<Graph<T>>();

        for (T n : map.keySet()) {

            if (bookkeeping.containsKey(n)) {
                // This node was already processed...
                continue;
            }

            ArrayList<T> connected_component =
                    this.strongConnect(n, explored_nodes, index, bookkeeping);

            if (connected_component == null) {
                continue;
            }

            // We found a connected component
            Graph<T> subgraph = new Graph<T>(connected_component.size());
            for (T node : connected_component) {
                subgraph.put(node, this.getNeighbors(node));
            }
            connected_components.add(subgraph);

        }

        return connected_components;
    }

    /**
     *
     * @param startingPoint
     * @param exploredNodes
     * connected component.
     * @param index
     * @param bookkeeping
     * @return
     */
    private ArrayList<T> strongConnect(
            final T startingPoint,
            final Stack<NodeParent> exploredNodes,
            final Index index,
            final HashMap<T, NodeProperty> bookkeeping) {


        // explored_nodes stores the history of nodes explored but not yet
        // assigned to a strongly connected component

        // use a stack to perform depth first search (DFS) without using
        // recursion
        final Stack<NodeParent> nodesToProcess = new Stack<NodeParent>();
        nodesToProcess.push(new NodeParent(startingPoint, null));


        while (!nodesToProcess.empty()) {
            NodeParent nodeAndParent = nodesToProcess.pop();
            T node = nodeAndParent.node;

            bookkeeping.put(
                    node,
                    new NodeProperty(index.value(), index.value()));
            index.inc();
            exploredNodes.add(nodeAndParent);

            // process neighbors of this node
            for (Neighbor<T> neighbor : this.getNeighbors(node)) {
                T neighbor_node = neighbor.getNode();

                if (!this.containsKey(neighbor_node)
                        || this.getNeighbors(neighbor_node) == null) {
                    // neighbor_node is actually part of another subgraph
                    // (this can happen during distributed processing)
                    // => skip
                    continue;
                }

                if (bookkeeping.containsKey(neighbor_node)) {
                    // this node was already processed
                    continue;
                }

                boolean skip = false;
                for (NodeParent node_in_queue : nodesToProcess) {
                    // Already in the queue for processing
                    if (node_in_queue.node.equals(neighbor_node)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }

                // Perform depth first search...
                nodesToProcess.push(new NodeParent(neighbor_node, node));
            }
        }

        // Traverse the stack of explored nodes to update the lowlink value of
        // each node
        for (NodeParent nodeAndParent : exploredNodes) {
            T node = nodeAndParent.parent;
            T child = nodeAndParent.node;

            if (node == null) {
                // this child is actually the starting point.
                continue;
            }

            bookkeeping.get(node).lowlink = Math.min(
                    bookkeeping.get(node).lowlink,
                    bookkeeping.get(child).lowlink);
        }

        for (NodeParent node_and_parent : exploredNodes) {
            T node = node_and_parent.node;
            if (bookkeeping.get(node).lowlink == bookkeeping.get(node).index) {
                // node is the root of a strongly connected component
                // => fetch and return all nodes in this component
                ArrayList<T> connectedComponent = new ArrayList<T>();

                T otherNode;
                do {
                    otherNode = exploredNodes.pop().node;
                    bookkeeping.get(otherNode).onstack = false;
                    connectedComponent.add(otherNode);
                } while (!startingPoint.equals(otherNode));
                return connectedComponent;
            }
        }

        return null;
    }

    /**
     * Store the node and it's parent.
     */
    private class NodeParent {
        private T node;
        private T parent;

        NodeParent(final T node, final T parent) {
            this.node = node;
            this.parent = parent;
        }
    }

    /**
     * Helper class to compute strongly connected components.
     */
    private static class Index {

        private int value;

        public int value() {
            return this.value;
        }

        public void inc() {
            this.value++;
        }
    }

    /**
     * Helper class to compute strongly connected components.
     */
    private static class NodeProperty {

        private int index;
        private int lowlink;
        private boolean onstack;

        NodeProperty(final int index, final int lowlink) {
            this.index = index;
            this.lowlink = lowlink;
            this.onstack = true;
        }
    };

    /**
     *
     * @param node
     * @param neighborlist
     * @return
     */
    public final NeighborList put(
            final T node, final NeighborList neighborlist) {
        return map.put(node, neighborlist);
    }

    /**
     *
     * @param node
     * @return
     */
    public final boolean containsKey(final T node) {
        return map.containsKey(node);
    }

    /**
     *
     * @return
     */
    public final int size() {
        return map.size();
    }

    /**
     *
     * @return
     */
    public final Iterable<Map.Entry<T, NeighborList>> entrySet() {
        return map.entrySet();
    }

    /**
     *
     * @return
     */
    public final ArrayList<T> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<T>(map.keySet());
        }

        return nodes;
    }

    /**
     * Recursively search neighbors of neighbors, up to a given depth.
     * @param startingPoints
     * @param depth
     * @return
     */
    public final LinkedList<T> findNeighbors(
            final LinkedList<T> startingPoints,
            final int depth) {
        LinkedList<T> neighbors = new LinkedList<T>(startingPoints);

        // I can NOT loop over candidates as I will add items to it inside the
        // loop!
        for (T startNode : startingPoints) {
            // As depth will be small, I can use recursion here...
            findNeighbors(neighbors, startNode, depth);
        }
        return neighbors;
    }

    private void findNeighbors(
            final LinkedList<T> candidates,
            final T node,
            final int currentDepth) {

        // With the distributed online algorithm, the nl might be null
        // because it is located on another partition
        NeighborList nl = getNeighbors(node);
        if (nl == null) {
            return;
        }

        for (Neighbor<T> n : nl) {
            if (!candidates.contains(n.getNode())) {
                candidates.add(n.getNode());
                if (currentDepth > 0) {
                    // don't use current_depth++ here as we will reuse it in
                    // the for loop !
                    findNeighbors(candidates, n.getNode(), currentDepth - 1);
                }
            }
        }
    }

    /**
     * Get the underlying hash map that stores the nodes and associated
     * neighborlists.
     * @return
     */
    public final HashMap<T, NeighborList> getHashMap() {
        return map;
    }

    /**
     * Multi-thread exhaustive search.
     * @param query
     * @param k
     * @return
     * @throws InterruptedException if thread is interrupted
     * @throws ExecutionException if thread cannot complete
     */
    public final NeighborList search(final T query, final int k)
            throws InterruptedException, ExecutionException {

        int procs = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(procs);
        List<Future<NeighborList>> results = new ArrayList();
        for (int i = 0; i < procs; i++) {
            int start = getNodes().size() / procs * i;
            int stop = Math.min(
                    getNodes().size() / procs * (i + 1), getNodes().size());
            results.add(pool.submit(new SearchTask(nodes, query, start, stop)));
        }

        // Reduce
        NeighborList neighbors = new NeighborList(k);
        for (Future<NeighborList> future : results) {
            neighbors.addAll(future.get());
        }
        pool.shutdown();
        return neighbors;
    }

    /**
     * Class used for multi-thread search.
     */
    private class SearchTask implements Callable<NeighborList> {
        private final ArrayList<T> nodes;
        private final T query;
        private final int start;
        private final int stop;
        SearchTask(
                final ArrayList<T> nodes,
                final T query,
                final int start,
                final int stop) {

            this.nodes = nodes;
            this.query = query;
            this.start = start;
            this.stop = stop;
        }

        @Override
        public NeighborList call() throws Exception {
            NeighborList nl = new NeighborList(k);
            for (int i = start; i < stop; i++) {
                T other = getNodes().get(i);
                nl.add(new Neighbor(
                        other,
                        similarity.similarity(query, other)));
            }
            return nl;

        }
    }

    /**
     * Approximate fast graph based search, as published in "Fast Online k-nn
     * Graph Building" by Debatty et al.
     *
     * @see <a href="http://arxiv.org/abs/1602.06819">Fast Online k-nn Graph
     * Building</a>
     * @param query
     * @return
     */
    public final FastSearchResult fastSearch(final T query) {
        return fastSearch(query, FastSearchConfig.getDefault());
    }

    /**
     * Approximate fast graph based search, as published in "Fast Online k-nn
     * Graph Building" by Debatty et al.
     *
     * @see <a href="http://arxiv.org/abs/1602.06819">Fast Online k-nn Graph
     * Building</a>
     *
     * @param query
     * @param conf
     * @return
     */
    public final FastSearchResult fastSearch(
            final T query,
            final FastSearchConfig conf) {

        return fastSearch(query, conf, getRandomNode());
    }

    /**
     * Approximate fast graph based search, as published in "Fast Online k-nn
     * Graph Building" by Debatty et al.
     *
     * @param conf
     * @param start starting point
     * @see <a href="http://arxiv.org/abs/1602.06819">Fast Online k-nn Graph
     * Building</a>
     * @param query query point
     *
     * @return
     */
    public final FastSearchResult fastSearch(
            final T query,
            final FastSearchConfig conf,
            final T start) {

        FastSearchResult result = new FastSearchResult(conf.getK());
        int maxSimilarities = (int) (map.size() / conf.getSpeedup());

        // Looking for more nodes than this graph contains...
        // Or fall back to exhaustive search
        if (conf.getK() >= map.size()
                || maxSimilarities >= map.size()) {

            for (T node : map.keySet()) {
                result.getNeighbors().add(
                        new Neighbor(
                                node,
                                similarity.similarity(query, node)));
                result.incSimilarities();
            }
            return result;
        }

        T currentNode = start;
        // Node => Similarity with query node
        // Max number of nodes we will visit is max_similarities
        HashMap<T, Double> visitedNodes = new HashMap<T, Double>(
                maxSimilarities);
        double highestSimilarity = 0;

        while (result.getSimilarities() < maxSimilarities) { // Restart...

            restart : {
                // Already been here => restart
                if (visitedNodes.containsKey(currentNode)) {
                    break restart;
                }

                // starting point too far (similarity too small) => restart!
                double currentNodeSimilarity = similarity.similarity(
                        query,
                        currentNode);
                result.incSimilarities();

                if (currentNodeSimilarity
                        < highestSimilarity / conf.getExpansion()) {
                    break restart;
                }

                // Follow the chain of neighbors
                while (result.getSimilarities() < maxSimilarities) {

                    NeighborList nl = this.getNeighbors(currentNode);

                    // Node has no neighbor (cross partition edge) => restart or
                    // return
                    if (nl == null) {
                        if (conf.isRestartAtBoundary()) {
                            result.incBoundaryRestarts();
                            break restart;

                        } else {
                            result.setBoundaryNode(currentNode);
                            return result;
                        }
                    }

                    T bestNeighbor = null;
                    double bestNeighborSimilarity = currentNodeSimilarity;

                    for (int i = 0; i < conf.getLongJumps(); i++) {
                        // Check a random node (to simulate long jumps)
                        T neighbor = getRandomNode();

                        // Already been here => skip
                        if (visitedNodes.containsKey(neighbor)) {
                            continue;
                        }

                        // Compute similarity to query
                        double neighborSimilarity = similarity.similarity(
                                query,
                                neighbor);
                        result.incSimilarities();
                        visitedNodes.put(neighbor, neighborSimilarity);

                        // If this node provides an improved similarity, keep it
                        if (neighborSimilarity > currentNodeSimilarity) {
                            bestNeighbor = neighbor;
                            bestNeighborSimilarity = neighborSimilarity;
                            // early break
                            break;
                        }
                    }

                    // Check the neighbors of current_node and try to find a
                    // node with higher similarity
                    for (Neighbor aNl : nl) {
                        T neighbor = (T) aNl.getNode();
                        if (visitedNodes.containsKey(neighbor)) {
                            continue;
                        }
                        // Compute similarity with query
                        double neighborSimilarity = similarity.similarity(
                                query,
                                neighbor);
                        result.incSimilarities();
                        visitedNodes.put(neighbor, neighborSimilarity);

                        // If this node provides an improved similarity, keep it
                        if (neighborSimilarity > bestNeighborSimilarity) {
                            bestNeighbor = neighbor;
                            bestNeighborSimilarity = neighborSimilarity;
                            // early break...
                            break;
                        }
                    }

                    // record the similarity of this node if
                    // it is the best seen so far
                    // (will be used with expansion parameter at next iteraion)
                    if (bestNeighborSimilarity > highestSimilarity) {
                        highestSimilarity = bestNeighborSimilarity;
                    }
                    // No node provides higher similarity
                    // => we reached the end of this track...
                    // => restart and
                    if (bestNeighbor == null) {
                        break restart;
                    }
                    currentNode = bestNeighbor;
                }
            } // restart
            currentNode = getRandomNode();
            result.incRestarts();
        }


        for (Map.Entry<T, Double> entry : visitedNodes.entrySet()) {
            result.getNeighbors().add(
                    new Neighbor(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * Writes the graph as a GEXF file (to be used in Gephi, for example).
     *
     * @param filename
     * @throws FileNotFoundException if filename is invalid
     * @throws IOException if cannot write to file
     */
    public final void writeGEXF(final String filename)
            throws FileNotFoundException, IOException {
        Writer out = new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(filename)));
        out.write(GEXF_HEADER);

        // Write nodes
        out.write("<nodes>\n");
        int nodeId = 0;
        Map<T, Integer> nodeRegistry = new IdentityHashMap<T, Integer>();

        for (T node : map.keySet()) {
            nodeRegistry.put(node, nodeId);
            out.write("<node id=\"" + nodeId
                    + "\" label=\"" + node.toString() + "\" />\n");
            nodeId++;
        }
        out.write("</nodes>\n");

        // Write edges
        out.write("<edges>\n");
        int i = 0;
        for (T source : map.keySet()) {
            int sourceId = nodeRegistry.get(source);
            for (Neighbor<T> target : this.getNeighbors(source)) {
                int targetId = nodeRegistry.get(target.getNode());
                out.write("<edge id=\"" + i + "\" source=\"" + sourceId + "\" "
                        + "target=\"" + targetId + "\" "
                        + "weight=\"" + target.getSimilarity() + "\" />\n");
                i++;
            }
        }

        out.write("</edges>\n");

        // End the file
        out.write("</graph>\n"
                + "</gexf>");
        out.close();
    }

    private static final String GEXF_HEADER
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">\n"
            + "<meta>\n"
            + "<creator>info.debatty.java.graphs.Graph</creator>\n"
            + "<description></description>\n"
            + "</meta>\n"
            + "<graph mode=\"static\" defaultedgetype=\"directed\">\n";



    /**
     * Add a node to the online graph using exhaustive search approach.
     * Adding a node requires to compute the similarity between the new node
     * and every other node in the graph...
     * @param newNode
     * @return
     */
    public final int add(final T newNode) {
        if (containsKey(newNode)) {
            throw new IllegalArgumentException(
                    "This graph already contains this node");
        }
        NeighborList nl = new NeighborList(k);
        for (T otherNode : getNodes()) {
            double sim = similarity.similarity(
                    newNode, otherNode);
            nl.add(new Neighbor(otherNode, sim));
            getNeighbors(otherNode).add(new Neighbor(newNode, sim));
        }
        this.put(newNode, nl);
        nodes = null;
        return (size() - 1);
    }

    /**
     * Add a node to the online graph, using approximate online graph building
     * algorithm presented in "Fast Online k-nn Graph Building" by Debatty
     * et al. Default speedup is 4 compared to exhaustive search.
     *
     * @param node
     */
    public final void fastAdd(final T node) {
        fastAdd(node, OnlineConfig.getDefault());
    }

    /**
     * Add a node to the online graph, using approximate online graph building
     * algorithm presented in "Fast Online k-nn Graph Building" by Debatty
     * et al.
     *
     * @param newNode
     * @param conf
     */
    public final void fastAdd(
            final T newNode,
            final OnlineConfig conf) {
        if (containsKey(newNode)) {
            throw new IllegalArgumentException(
                    "This graph already contains this node");
        }
        // 3. Search the neighbors of the new node
        conf.setK(getK());
        NeighborList neighborlist = fastSearch(newNode, conf).getNeighbors();
        put(newNode, neighborlist);
        // 4. Update existing edges
        // Nodes to analyze at this iteration
        LinkedList<T> analyze = new LinkedList<T>();

        // Nodes to analyze at next iteration
        LinkedList<T> nextAnalyze = new LinkedList<T>();

        // List of already analyzed nodes
        HashMap<T, Boolean> visited = new HashMap<T, Boolean>();

        // Fill the list of nodes to analyze
        for (Neighbor<T> neighbor : getNeighbors(newNode)) {
            analyze.add(neighbor.getNode());
        }

        StatisticsContainer stats = new StatisticsContainer();

        for (int d = 0; d < conf.getUpdateDepth(); d++) {
            while (!analyze.isEmpty()) {
                T other = analyze.pop();
                NeighborList otherNeighborlist = getNeighbors(other);
                // Add neighbors to the list of nodes to analyze at
                // next iteration
                for (Neighbor<T> otherNeighbor : otherNeighborlist) {
                    if (!visited.containsKey(otherNeighbor.getNode())) {
                        nextAnalyze.add(otherNeighbor.getNode());
                    }
                }
                // Try to add the new node (if sufficiently similar)
                stats.incAddSimilarities();
                otherNeighborlist.add(new Neighbor(
                        newNode,
                        similarity.similarity(
                                newNode,
                                other)));

                visited.put(other, Boolean.TRUE);
            }
            analyze = nextAnalyze;
            nextAnalyze = new LinkedList<T>();
        }

        nodes = null;
    }

    /**
     * Remove a node from the graph (and update the graph) using fast
     * approximate algorithm.
     * @param nodeToRemove
     */
    public final void fastRemove(final T nodeToRemove) {
        fastRemove(nodeToRemove, OnlineConfig.getDefault());
    }

    /**
     * Remove a node from the graph (and update the graph) using fast
     * approximate algorithm.
     * @param nodeToRemove
     * @param conf
     */
    public final void fastRemove(
            final T nodeToRemove,
            final OnlineConfig conf) {

        // Build the list of nodes to update
        LinkedList<T> nodesToUpdate = new LinkedList<T>();

        for (T node : getNodes()) {
            NeighborList nl = getNeighbors(node);
            if (nl.containsNode(nodeToRemove)) {
                nodesToUpdate.add(node);
                nl.removeNode(nodeToRemove);
            }
        }

        // Build the list of candidates
        LinkedList<T> initialCandidates = new LinkedList<T>();
        initialCandidates.add(nodeToRemove);
        initialCandidates.addAll(nodesToUpdate);

        LinkedList<T> candidates = findNeighbors(
                initialCandidates, conf.getUpdateDepth());
        while (candidates.contains(nodeToRemove)) {
            candidates.remove(nodeToRemove);
        }

        StatisticsContainer stats = new StatisticsContainer();

        // Update the nodes_to_update
        for (T nodeToUpdate : nodesToUpdate) {
            NeighborList nlToUpdate = getNeighbors(nodeToUpdate);
            for (T candidate : candidates) {
                if (candidate.equals(nodeToUpdate)) {
                    continue;
                }

                stats.incRemoveSimilarities();
                double sim = similarity.similarity(
                        nodeToUpdate,
                        candidate);

                nlToUpdate.add(new Neighbor(candidate, sim));
            }
        }

        // Remove node_to_remove
        map.remove(nodeToRemove);
        nodes = null;
    }

    /**
     * Count the number of edges/neighbors that are the same (based on
     * similarity) in both graphs.
     *
     * @param other
     * @return
     */
    public final int compare(final Graph<T> other) {
        int correctEdges = 0;
        for (T node : map.keySet()) {
            correctEdges += getNeighbors(node).countCommons(
                    other.getNeighbors(node));
        }
        return correctEdges;
    }

    @Override
    public final String toString() {
        return map.toString();
    }

    private static final int HASH_BASE = 3;
    private static final int HASH_MULT = 23;

    @Override
    public final int hashCode() {
        int hash = HASH_BASE;
        hash = HASH_MULT * hash + this.map.hashCode();
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Graph<?> other = (Graph<?>) obj;

        return this.map.equals(other.map);
    }
}
