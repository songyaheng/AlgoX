package com.github.algox.indexsearch;

public class Neighbor implements Comparable<Neighbor>{
    public int id;
    public double distance;
    public boolean flag;

    public Neighbor() {
    }

    public Neighbor(int id, double distance, boolean flag) {
        this.id = id;
        this.distance = distance;
        this.flag = flag;
    }

    @Override
    public int compareTo(Neighbor o) {
        return Double.compare(this.distance, o.distance);
    }
}
