package com.github.algox.indexsearch;

import org.nd4j.linalg.api.rng.Random;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {
    public static void genRandom(Random random, List<Integer> addr, int size, int n) {
        for (int i = 0; i < size; ++ i) {
            addr.add(random.nextInt() % (n - size));
        }
        addr.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        for (int i = 1; i < size; ++ i) {
            if (addr.get(i) <= addr.get(i - 1)) {
                addr.set(i, addr.get(i -1) + 1);
            }
        }
        int off = random.nextInt() % n;
        for (int i = 0; i < size; ++ i) {
            addr.set(i, (addr.get(i) + off) % n);
        }
    }

    public static List<Neighbor> partSort(List<Neighbor>  list, int size) {
        TopK pq = new TopK<Neighbor>(size);
        for (Neighbor neighbor: list) {
            pq.add(neighbor);
        }
        List<Neighbor> neighbors = pq.sortedList();
        Set<Integer> set = neighbors.stream().map(n -> n.id).collect(Collectors.toSet());
        neighbors.addAll(list.stream().filter(n -> !set.contains(n.id)).collect(Collectors.toList()));
        return neighbors;
    }
}
