package com.github.algox.indexsearch;

import java.util.*;

public class TopK<E extends Comparable> {
    private PriorityQueue<E> queue;
    private int maxSize; //堆的最大容量

    public TopK(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalStateException();
        }
        this.maxSize = maxSize;
        this.queue = new PriorityQueue<>(maxSize, new Comparator<E>() {
            @Override
            public int compare(E o1, E o2) {
                // 最大堆用o2 - o1，最小堆用o1 - o2
                return o2.compareTo(o1);
            }
        });
    }

    public void add(E e) {
        if (queue.size() < maxSize) {
            queue.add(e);
        } else {
            E peek = queue.peek();
            if (peek.compareTo(e) > 0) {
                queue.poll();
                queue.add(e);
            }
        }
    }

    public List<E> sortedList() {
        List<E> list = new ArrayList<>(queue);
        Collections.sort(list);
        return list;
    }
}
