package com.github.algox.indexsearch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nd4j.linalg.api.rng.Random;
import org.nd4j.linalg.cpu.nativecpu.rng.CpuNativeRandom;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class NsgIndex {
    private static List<List<Integer>> finalGraph;
    private List<double[]> data;
    private int r;
    private int dim;
    private int nd;
    private int l;
    private int ep;
    private int c;
    private int width;
    private Distance distance;
    private Random random = new CpuNativeRandom();

    public NsgIndex(List<double[]> data) {
        this.data = data;
        this.nd = data.size();
    }

    public void setR(int r) {
        this.r = r;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public void setL(int l) {
        this.l = l;
    }

    public void setC(int c) {
        this.c = c;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    private void initNsgGarph() {
        double[] center = new double[dim];
        for (int i = 0; i < nd; i ++) {
            for (int j = 0; j < dim; j ++) {
                center[j] = center[j] + data.get(i)[j];
            }
        }
        for (int j = 0; j < dim; j ++) {
            center[j] = center[j] / nd;
        }
        List<Neighbor> tmp = new ArrayList<>(l);
        for (int i = 0; i < l; i ++) {
            tmp.add(new Neighbor());
        }
        List<Neighbor> pool = new ArrayList<>();
        ep = random.nextInt() % nd;
        getNeighbors(center, tmp, pool);
        ep = tmp.get(0).id;
    }

    private void getNeighbors(double[] query, List<Neighbor> retset, List<Neighbor> fullset) {
        int[] initIds = new int[l];
        boolean[] flags = new boolean[nd];
        int ll = 0;
        for (int i = 0; i < initIds.length && i < finalGraph.get(ep).size(); i ++) {
            initIds[i] = finalGraph.get(ep).get(i);
            flags[initIds[i]] = true;
            ll ++;
        }
        while (ll < initIds.length) {
            int id = random.nextInt() % nd;
            if (flags[id]) continue;
            ll ++;
            flags[id] = true;
        }
        ll = 0;
        for (int i = 0; i < initIds.length; i ++) {
            int id = initIds[i];
            if (id >= nd) continue;
            double dist = distance.distance(query, data.get(id));
            retset.set(i, new Neighbor(id, dist, true));
            ll++;
        }
        retset.sort(new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor o1, Neighbor o2) {
                return o1.compareTo(o2);
            }
        });
        int k = 0;
        while (k < ll) {
            int  nk = ll;
            if (retset.get(k).flag) {
                retset.get(k).flag = false;
                int n = retset.get(k).id;
                for (int id: finalGraph.get(n)) {
                    if (flags[id]) continue;
                    flags[id] = true;
                    double dist = distance.distance(query, data.get(id));
                    Neighbor nn = new Neighbor(id, dist, true);
                    fullset.add(nn);
                    if (dist >= retset.get(ll -1).distance) continue;
                    int rr = insertIntoPool(retset, ll, nn);
                    if (ll + 1 < retset.size()) ++ll;
                    if (rr < nk) nk = rr;
                }
            }
            if (nk <= k) {
                k = nk;
            } else {
                ++k;
            }
        }
    }

    private int insertIntoPool(List<Neighbor> addr, int k, Neighbor nn) {
        int left = 0;
        int right = k - 1;
        if (addr.get(left).distance > nn.distance) {
            addr.add(left, nn);
            addr.remove(addr.size() - 1);
            return left;
        }
        if (addr.get(right).distance < nn.distance) {
            addr.set(k, nn);
            return k;
        }
        while (left < right - 1) {
            int mid = (left + right) / 2;
            if (addr.get(mid).distance > nn.distance) {
                right = mid;
            } else {
                left = mid;
            }
        }

        while (left > 0) {
            if (addr.get(left).distance < nn.distance) break;
            if (addr.get(left).id == nn.id) return k + 1;
            left --;
        }
        if (addr.get(left).id == nn.id || addr.get(right).id == nn.id) return k + 1;
        addr.add(right, nn);
        addr.remove(addr.size() - 1);
        return right;
    }

    public void link(List<SimpleNeighbor[]> cutGraph) {
        List<Neighbor> pool = new ArrayList<>();
        List<Neighbor> tmp = new ArrayList<>(l);
        for (int i = 0; i < l; i ++) {
            tmp.add(new Neighbor());
        }
        boolean[] flags;
        for (int i = 0; i < nd; ++ i) {
            pool.clear();
            for (int j = 0; j < l; j ++) {
                tmp.set(j, new Neighbor());
            }
            flags = new boolean[nd];
            getNeighbors(data.get(i), flags, tmp, pool);
            syncPrune(i, pool, flags, cutGraph);
        }
        for (int n = 0; n < nd;  ++ n) {
            insertInto(n, r, cutGraph);
        }
    }

    private void insertInto(int n, int range, List<SimpleNeighbor[]> cutGraph) {
        for (int i = 0; i < range; i ++) {
            if (cutGraph.get(n)[i].distance == -1) break;
            SimpleNeighbor sn = new SimpleNeighbor(n, cutGraph.get(n)[i].distance);
            int des = cutGraph.get(n)[i].id;
            List<SimpleNeighbor> tmpPool = new ArrayList<>();
            boolean dup = false;
            for (int j = 0; j < range; j ++) {
                if (cutGraph.get(des)[j].distance == -1) break;
                if (n == cutGraph.get(des)[j].id) {
                    dup = true;
                    break;
                }
                tmpPool.add(cutGraph.get(des)[j]);
            }
            if (dup) continue;
            tmpPool.add(sn);
            if (tmpPool.size() > range) {
                List<SimpleNeighbor> result = new ArrayList<>();
                int start = 0;
                tmpPool.sort(new Comparator<SimpleNeighbor>() {
                    @Override
                    public int compare(SimpleNeighbor o1, SimpleNeighbor o2) {
                        return o1.compareTo(o2);
                    }
                });
                result.add(tmpPool.get(start));
                while (result.size() < range && (++start) < tmpPool.size()) {
                    SimpleNeighbor p = tmpPool.get(start);
                    boolean occlude = false;
                    for (SimpleNeighbor simpleNeighbor : result) {
                        if (p.id == simpleNeighbor.id) {
                            occlude = true;
                            break;
                        }
                        double djk = distance.distance(data.get(simpleNeighbor.id), data.get(p.id));
                        if (djk < p.distance) {
                            occlude = true;
                            break;
                        }
                    }
                    if (!occlude) {
                        result.add(p);
                    }
                }
                for (int t = 0; t < result.size(); t ++) {
                    cutGraph.get(des)[t] = result.get(t);
                }
            } else {
                for (int t = 0; t < range; t ++) {
                    if (cutGraph.get(des)[t].distance == -1) {
                        cutGraph.get(des)[t] = sn;
                        if (t + 1 < range) {
                            cutGraph.get(des)[t + 1] = new SimpleNeighbor(0, -1);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void syncPrune(int q, List<Neighbor> pool, boolean[] flags, List<SimpleNeighbor[]> cutGraph) {
        int range = r;
        int maxc = c;
        width = range;
        int start = 0;
        for (int nn = 0; nn < finalGraph.get(q).size(); nn ++) {
            int id = finalGraph.get(q).get(nn);
            if (flags[id]) continue;
            double dist = distance.distance(data.get(q), data.get(id));
            pool.add(new Neighbor(id, dist, true));
        }
        pool.sort(new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor o1, Neighbor o2) {
                return o1.compareTo(o2);
            }
        });
        List<Neighbor> result = new ArrayList<>();
        if (pool.get(start).id == q) start ++;
        result.add(pool.get(start));

        while (result.size() < range && (++ start) < pool.size() && start < maxc) {
            Neighbor p = pool.get(start);
            boolean occlude = false;
            for (Neighbor neighbor : result) {
                if (p.id == neighbor.id) {
                    occlude = true;
                    break;
                }
                double djk = distance.distance(data.get(neighbor.id), data.get(p.id));
                if (djk < p.distance) {
                    occlude = true;
                    break;
                }
            }
            if (!occlude) {
                result.add(p);
            }
        }
        SimpleNeighbor[] neighbors = new SimpleNeighbor[r];
        for (int t = 0; t < result.size(); t ++) {
            neighbors[t] = new SimpleNeighbor(result.get(t).id, result.get(t).distance);
        }
        if (result.size() < range) {
            neighbors[result.size()] = new SimpleNeighbor(0, -1);
        }
        cutGraph.add(neighbors);
    }


    public void build(String path) throws IOException {
        loadNNGraph(path);
        initNsgGarph();
        List<SimpleNeighbor[]> cutGraph = new ArrayList<>();
        link(cutGraph);
        for (int i = 0; i < nd; i ++) {
            int poolSize = 0;
            for (int j = 0; j < r; j ++) {
                if (cutGraph.get(i)[j].distance == -1) break;
                poolSize = j;
            }
            poolSize ++;
            finalGraph.set(i, finalGraph.get(i).subList(0, poolSize));
            for (int j = 0; j < poolSize; j ++) {
                finalGraph.get(i).set(j, cutGraph.get(i)[j].id);
            }
        }
        treeGrow();
        double max = 0;
        double min = 1e6;
        double avg = 0;
        for (int i = 0; i < nd; i ++) {
            int size = finalGraph.get(i).size();
            max = max < size ? size : max;
            min = min > size ? size : min;
            avg += size;
        }
        avg = avg / nd;
        System.out.println(String.format("Degree Statistics: Max = %f, Min = %f, Avg = %f\n", max, min, avg));
    }


    private void getNeighbors(double[] query, boolean[] flags, List<Neighbor> retset, List<Neighbor> fullSet) {
        int[] initIds = new int[l];
        int ll = 0;
        for (int i = 0; i < initIds.length && i < finalGraph.get(ep).size(); i ++) {
            initIds[i] = finalGraph.get(ep).get(i);
            flags[initIds[i]] = true;
            ll ++;
        }
        while (ll < initIds.length) {
            int id = random.nextInt() % nd;
            if (flags[id]) continue;
            initIds[ll] = id;
            ll ++;
            flags[id] = true;
        }

        ll = 0;
        for (int i = 0; i < initIds.length; i ++) {
            int id = initIds[i];
            if (id >= nd) continue;
            double dist = distance.distance(data.get(id), query);
            retset.set(i, new Neighbor(id, dist, true));
            fullSet.add(retset.get(i));
            ll ++;
        }
        retset.sort(new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor o1, Neighbor o2) {
                return o1.compareTo(o2);
            }
        });
        int k = 0;
        while (k < ll) {
            int nk = ll;
            if (retset.get(k).flag) {
                retset.get(k).flag = false;
                int n = retset.get(k).id;
                for (int id : finalGraph.get(n)) {
                    if (flags[id]) continue;
                    flags[id] = true;
                    double dist = distance.distance(data.get(id), query);
                    Neighbor nn = new Neighbor(id, dist, true);
                    fullSet.add(nn);
                    if (dist >= retset.get(ll - 1).distance) continue;
                    int rr = insertIntoPool(retset, ll, nn);
                    if (ll + 1 < retset.size()) ++ll;
                    if (rr < nk) nk = rr;
                }
            }
            if (nk <= k) {
                k = nk;
            } else {
                ++ k;
            }
        }
    }

    private void treeGrow() {
        int root = ep;
        boolean[] flags = new boolean[nd];
        int unlinkedCnt = 0;
        while (unlinkedCnt < nd) {
            unlinkedCnt = dfs(flags, root, unlinkedCnt);
            if (unlinkedCnt >= nd) break;
            findRoot(flags, root);
        }
        for (int i = 0; i < nd; ++ i) {
            if (finalGraph.get(i).size() > width) {
                width = finalGraph.get(i).size();
            }
        }
    }

    private void findRoot(boolean[] flags, int root) {
        int id = nd;
        for (int i = 0; i < nd; i ++) {
            if (!flags[i]) {
                id = i;
                break;
            }
        }
        if (id == nd) return;
        List<Neighbor> tmp = new ArrayList<>(l);
        for (int i = 0; i > l; i ++) {
            tmp.add(new Neighbor());
        }
        List<Neighbor> pool = new ArrayList<>();
        getNeighbors(data.get(id), tmp, pool);
        pool.sort(new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor o1, Neighbor o2) {
                return o1.compareTo(o2);
            }
        });
        boolean found = false;
        for (Neighbor neighbor : pool) {
            if (flags[neighbor.id]) {
                root = neighbor.id;
                found = true;
                break;
            }
        }
        if (found) {
            while (true) {
                int rid = random.nextInt() % nd;
                if (flags[rid]) {
                    root = rid;
                    break;
                }
            }
        }
        finalGraph.get(root).add(id);
    }

    private int dfs(boolean[] flags, int root, int cnt) {
        int c = cnt;
        int tmp = root;
        Stack<Integer> s = new Stack<>();
        s.push(root);
        if (!flags[root]) c ++;
        flags[root] = true;
        while (!s.empty()) {
            int next = nd + 1;
            for (int i : finalGraph.get(tmp)) {
                if (!flags[i]) {
                    next = i;
                    break;
                }
            }
            if (next == (nd + 1)) {
                s.pop();
                if (s.empty()) break;
                tmp = s.peek();
                continue;
            }
            tmp = next;
            flags[tmp] = true;
            s.push(tmp);
            c ++;
        }
        return c;
    }


    private void loadNNGraph(String path) throws IOException {
        finalGraph = new ArrayList<>();
        FileInputStream in = FileUtils.openInputStream(new File(path));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String tempString = null;
        while ((tempString = reader.readLine()) != null) {
            finalGraph.add(Arrays.stream(StringUtils.split(tempString, " ")).map(Integer::valueOf).collect(Collectors.toList()));
        }
        reader.close();
    }

    public void save(String path) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new File(path));
        writer.write(width + "\n");
        writer.write(ep + "\n");
        for (List<Integer> d : finalGraph) {
            writer.write(StringUtils.join(d, " ") + "\n");
        }
        writer.close();
    }

    public void load(String path) throws IOException {
        FileInputStream in = FileUtils.openInputStream(new File(path));
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader rd = new BufferedReader(reader);
        finalGraph = new ArrayList<>(nd);
        String line;
        int i = 0;
        while ((line = rd.readLine()) != null) {
            if (i == 0) {
                width = Integer.parseInt(line);
                i ++;
                continue;
            }
            if (i == 1) {
                ep = Integer.parseInt(line);
                i ++;
                continue;
            }
            List<Integer> list = Arrays.stream(StringUtils.split(line, " ")).map(Integer::parseInt).collect(Collectors.toList());
            finalGraph.add(list);
            i ++;
        }
        rd.close();
        in.close();
    }

    public List<Integer> search(double[] query, int pl) {
        int p = Math.min(pl , finalGraph.get(ep).size());
        List<Neighbor> retset = new ArrayList<>(pl);
        for (int i = 0; i < pl; i ++) {
            retset.add(new Neighbor());
        }
        int[] initIds = new int[pl];
        boolean[] flags = new boolean[nd];
        int tmp_l = 0;
        for (; tmp_l < p; tmp_l ++) {
            initIds[tmp_l] = finalGraph.get(ep).get(tmp_l);
            flags[initIds[tmp_l]] = true;
        }
        while (tmp_l < pl) {
            int id = random.nextInt() % nd;
            if (flags[id]) continue;
            flags[id] = true;
            initIds[tmp_l] = id;
            tmp_l ++;
        }

        for (int i = 0; i < initIds.length;  i ++) {
            int id = initIds[i];
            double dist = distance.distance(query, data.get(id));
            retset.set(i, new Neighbor(id, dist, true));
        }
        retset.sort(new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor o1, Neighbor o2) {
                return o1.compareTo(o2);
            }
        });
        int k = 0;
        while (k < pl) {
            int nk = pl;
            Neighbor nnk = retset.get(k);
            if (nnk.flag) {
                nnk.flag = false;
                retset.set(k, nnk);
                int n = nnk.id;
                for (int id: finalGraph.get(n)) {
                    if (flags[id]) continue;
                    flags[id] = true;
                    double dist = distance.distance(query, data.get(id));
                    if (dist >= retset.get(pl - 1).distance) continue;
                    Neighbor nn = new Neighbor(id, dist, true);
                    int r = insertIntoPool(retset, pl, nn);
                    if (r < nk) nk = r;
                }
            }
            if (nk <= k) {
                k = nk;
            } else {
                ++ k;
            }
        }
        return retset.stream().map(n -> n.id).collect(Collectors.toList());
    }

}
