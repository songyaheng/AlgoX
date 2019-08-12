package com.github.algox.indexsearch;

import org.apache.commons.lang3.StringUtils;
import org.nd4j.linalg.api.rng.Random;
import org.nd4j.linalg.cpu.nativecpu.rng.CpuNativeRandom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class IndexGraph {
    private static final int CONTROL_NUM = 100;
    List<double[]> data;
    private static Nhood[] graph;
    private static List<List<Integer>> finalGraph;
    private int nd;
    private int l;
    private int s;
    private int r;
    private int k;
    private int iter;
    private Distance distance;

    public void setNd(int nd) {
        this.nd = nd;
    }

    public void setL(int l) {
        this.l = l;
    }

    public void setS(int s) {
        this.s = s;
    }

    public void setR(int r) {
        this.r = r;
    }

    public void setK(int k) {
        this.k = k;
    }

    public void setIter(int iter) {
        this.iter = iter;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public void fromData(List<double[]> data) {
        this.data = data;
    }

    private void initGraph() {
        graph = new Nhood[nd];
        Random random = new CpuNativeRandom();
        for (int i = 0; i < nd; i ++) {
            graph[i] = new Nhood(l, s, random, nd);
        }
        for (int i = 0; i < nd; i ++) {
            List<Integer> tmp = new ArrayList<>(s);
            Utils.genRandom(random, tmp, s + 1, nd);
            for (int j = 0; j < s; j ++) {
                int id = tmp.get(j);
                if (id == i) {
                    continue;
                }
                double dist = distance.distance(data.get(i), data.get(id));
                graph[i].pool.add(new Neighbor(id, dist, true));
            }
            graph[i].makeHeap();
        }
    }

    public void join() {
        for (int i = 0; i < nd; i ++) {
            graph[i].join(new Callback() {
                @Override
                public void call(int i, int j) {
                    if (i != j) {
                        double dist = distance.distance(data.get(i), data.get(j));
                        graph[i].insert(j, dist);
                        graph[j].insert(i, dist);
                    }
                }
            });
        }
    }

    public void evalRecall(List<Integer> controlPoints, int[][] accEvalSet) {
        double meanAcc = 0;
        for (int i = 0; i < controlPoints.size(); i ++) {
            double acc = 0;
            List<Neighbor> g = graph[controlPoints.get(i)].pool;
            int[] v = accEvalSet[i];
            for (Neighbor neighbor: g) {
                for (int k : v) {
                    if (neighbor.id == k) {
                        acc ++;
                        break;
                    }
                }
            }
            meanAcc += acc / v.length;
        }
        System.out.println("recall: " + (meanAcc / controlPoints.size()));
    }

    public void nnDescent() {
        Random random = new CpuNativeRandom();
        List<Integer> controlPoints = new ArrayList<>(CONTROL_NUM);
        Utils.genRandom(random, controlPoints, CONTROL_NUM, nd);
//        int[][] accEvalSet = generateControlSet(controlPoints);
        for (int it = 0; it < iter; it ++) {
            join();
            update();
//            evalRecall(controlPoints, accEvalSet);
            System.out.println("iter: " + it);
        }
    }

    public void update() {
        Random rnd = new CpuNativeRandom();
        for (int i = 0; i < nd; i ++) {
            graph[i].nnNew.clear();
            graph[i].nnOld.clear();
        }
        for (int i = 0; i < nd; ++ i) {
            graph[i].sortPool();
            int maxl = Math.min(graph[i].m + s, graph[i].pool.size());
            int c = 0;
            int ll = 0;
            while ((ll < maxl) && (c < s)) {
                if (graph[i].pool.get(ll).flag) {
                    ++c;
                }
                ++ ll;
            }
            graph[i].m = ll;
        }
        for (int i = 0; i < nd; ++i) {
            Nhood nnhd = graph[i];
            for (int j = 0; j < nnhd.m; ++ j) {
                Neighbor nn = nnhd.pool.get(j);
                Nhood nhood = graph[nn.id];
                if (nn.flag) {
                    graph[i].nnNew.add(nn.id);
                    if (nn.distance > nhood.pool.get(nhood.pool.size() - 1).distance) {
                        if (nhood.rnnNew.size() < r) {
                            graph[nn.id].rnnNew.add(i);
                        } else {
                            int pos = rnd.nextInt(Integer.MAX_VALUE) % r;
                            graph[nn.id].rnnNew.set(pos, i);
                        }
                    }
                    graph[i].pool.get(j).flag = false;
                } else {
                    graph[i].nnOld.add(nn.id);
                    if (nn.distance > nhood.pool.get(nhood.pool.size() - 1).distance) {
                        if (nhood.rnnOld.size() < r) {
                            graph[nn.id].rnnOld.add(i);
                        } else {
                            int pos = rnd.nextInt(Integer.MAX_VALUE) % r;
                            graph[nn.id].rnnOld.set(pos, i);
                        }
                    }
                }
            }
            graph[i].makeHeap();
        }
        for (int i = 0; i < nd; ++i) {
            if (r > 0 && graph[i].rnnNew.size() > r) {
                graph[i].shuffleRnnNew();
            }
            graph[i].nnNew.addAll(graph[i].rnnNew);
            if (r > 0 && graph[i].rnnOld.size() > r) {
                graph[i].shuffleRnnOld();
            }
            graph[i].nnOld.addAll(graph[i].rnnOld);
            if (graph[i].nnOld.size() > 2 * r) {
                graph[i].nnOld = graph[i].nnOld.subList(0, 2 * r);
            }
            graph[i].rnnNew.clear();
            graph[i].rnnOld.clear();
        }
    }

    public int[][] generateControlSet(List<Integer> controlPoints) {
        int[][] accEvalSet = new int[controlPoints.size()][CONTROL_NUM];
        for (int i = 0; i < controlPoints.size(); i ++) {
            List<Neighbor> tmp = new ArrayList<>(nd);
            for (int j = 0; j < nd; j ++) {
                double dist = distance.distance(data.get(controlPoints.get(i)), data.get(j));
                tmp.add(new Neighbor(j, dist, true));
            }
            Collections.sort(tmp);
            for (int j = 0; j < CONTROL_NUM; j ++) {
                accEvalSet[i][j] = tmp.get(j).id;
            }
        }
        return accEvalSet;
    }

    public void build(){
        initGraph();
        nnDescent();
        finalGraph = new ArrayList<>(nd);
        for (int i = 0; i < nd; i ++) {
            List<Integer> tmp = new ArrayList<>(k);
            graph[i].sortPool();
            for (int j =0; j < k; j ++) {
                tmp.add(graph[i].pool.get(j).id);
            }
            finalGraph.add(tmp);
            graph[i].pool.clear();
            graph[i].nnNew.clear();
            graph[i].nnOld.clear();
            graph[i].rnnNew.clear();
            graph[i].rnnOld.clear();
        }
        System.out.println("Build OK");
    }

    public void save(String path) {
        try {
            PrintWriter writer = new PrintWriter(new File(path));
            for (List<Integer> d : finalGraph) {
                writer.write(StringUtils.join(d, " ") + "\n");
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
