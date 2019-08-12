package com.github.algox.indexsearch;

import org.nd4j.linalg.api.rng.Random;

import java.util.*;

public class Nhood {
    public List<Neighbor> pool;
    public int m;
    public int poolCapacity;
    public List<Integer> nnOld = new ArrayList<>();
    public List<Integer> nnNew;
    public List<Integer> rnnOld = new ArrayList<>();
    public List<Integer> rnnNew = new ArrayList<>();

    public Nhood(int l, int s, Random random, int n) {
        this.m = s;
        this.nnNew = new ArrayList<>(s * 2);
        Utils.genRandom(random, nnNew, s *2, n);
        this.pool = new ArrayList<>(l);
        poolCapacity = l;
    }

    public void sortPool() {
        this.pool.sort(new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor o1, Neighbor o2) {
                return o1.compareTo(o2);
            }
        });
    }

    public void insert(int id, double dist) {
        if (dist > pool.get(0).distance) return;
        for (Neighbor i: pool) {
            if (id == i.id) return;
        }
        if (pool.size() < poolCapacity) {
            pool.add(new Neighbor(id, dist, true));
            makeHeap();
        } else {
            pool.remove(0);
            makeHeap();
            pool.add(new Neighbor(id, dist, true));
            makeHeap();
        }
    }


    public void join(Callback callback) {
        for (Integer i : nnNew) {
            for (Integer j : nnNew) {
                if (i < j) {
                    callback.call(i, j);
                }
            }
            for (Integer j : nnOld) {
                callback.call(i, j);
            }
        }
    }

    public void makeHeap() {
        PriorityQueue<Neighbor> maxHeap = new PriorityQueue<Neighbor>(poolCapacity, new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor o1, Neighbor o2) {
                return o2.compareTo(o1);
            }
        });
        maxHeap.addAll(pool);
        pool = new ArrayList<>(maxHeap);
    }

    public void shuffleRnnNew() {
        Collections.shuffle(rnnNew);
    }

    public void shuffleRnnOld() {
        Collections.shuffle(rnnOld);
    }
}
