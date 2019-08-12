package com.github.algox.indexsearch;

import com.github.algox.commons.Processor;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IndexMain {
    public static void main(String[] args) {
        List<double[]> data = new ArrayList<>();
        read(args[0], new Processor<String, double[]>() {
            @Override
            public double[] process(String s) {
                double[] vec = new double[100];
                try {
                    String[] sp = StringUtils.split(s, ",");
                    for (int i = 0; i < 100; i ++) {
                        vec[i] = Double.parseDouble(sp[i]);
                    }
                } catch (Exception e) {
                }
                data.add(vec);
                return vec;
            }
        });
        IndexGraph indexGraph = new IndexGraph();
        indexGraph.fromData(data);
        indexGraph.setL(200);
        indexGraph.setNd(data.size());
        indexGraph.setS(10);
        indexGraph.setR(100);
        indexGraph.setIter(10);
        indexGraph.setK(200);
        indexGraph.setDistance(new DotDistance());
        indexGraph.build();
        indexGraph.save(args[1]);
    }

    public static void read(String Path, Processor<String, double[]> process) {
        BufferedReader reader = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(Path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                if (StringUtils.isNoneEmpty(tempString)) {
                    process.process(tempString);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
