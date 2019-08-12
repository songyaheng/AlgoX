package com.github.algox.indexsearch;

public class CommonDistance implements Distance {
    @Override
    public double distance(double[] v1, double[] v2) {
        double v = 0.0;
        for (int i = 0; i < v1.length; i ++) {
            v = v +  (v1[i] - v2[i]) * (v1[i] - v2[i]);
        }
        return v;
    }
}
