package com.github.algox.indexsearch;

public class SimpleNeighbor implements Comparable<SimpleNeighbor>{
    public int id;
    public double distance;

    public SimpleNeighbor(int id, double distance) {
        this.id = id;
        this.distance = distance;
    }

    @Override
    public int compareTo(SimpleNeighbor o) {
        return Double.compare(this.distance, o.distance);
    }
}
