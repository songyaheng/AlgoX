package com.github.algox.indexsearch;

public class DotDistance  implements Distance {
    @Override
    public double distance(double[] v1, double[] v2) {
        double v = 0;
        for (int i = 0; i < v1.length; i ++) {
            v += v1[i] * v2[i];
        }
        return 1 - v;
    }
}
