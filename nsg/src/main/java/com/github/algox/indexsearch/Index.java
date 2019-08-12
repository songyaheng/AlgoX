package com.github.algox.indexsearch;

import com.alibaba.fastjson.JSON;
import com.github.algox.utils.BytesUtils;
import com.google.common.primitives.Bytes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Index {
    public static void main(String[] args) throws IOException {
        List<double[]> data = new ArrayList<>();
//        int num = 0;
//        int dim = 0;
//        try(FileChannel fc = new RandomAccessFile("/Users/songyaheng/Downloads/siftsmall/siftsmall_base.fvecs", "rw").getChannel()) {
//            ByteBuffer fb = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size())
//                    .order(ByteOrder.nativeOrder()).asReadOnlyBuffer();
//            byte[] b = new byte[4];
//            fb.get(b);
//            Bytes.reverse(b);
//            dim = BytesUtils.byteArrToInt(b);
//            System.out.println("data dimension: " + dim);
//            int fsize = fb.limit();
//            num = (fsize / (dim+1) / 4);
//            for (int i = 0; i < num; i++) {
//                double[] vec = new double[dim];
//                for (int j = 0; j < dim; j ++) {
//                    fb.get(b);
//                    Bytes.reverse(b);
//                    vec[j] = BytesUtils.byteArrToFloat(b);
//                }
//                data.add(vec);
//                if (fb.hasRemaining()) {
//                    fb.get(b);
//                }
//            }
//        }

        Map<Integer, String> map = new HashMap<>();
        int dim = 200;
        final AtomicInteger count = new AtomicInteger(0);
        readFileByLines("/Users/songyaheng/Downloads/result.txt", new Process<String, String>() {
            @Override
            public String process(String s) {
                String[] sp = StringUtils.split(s, "&&");
                String key = sp[0];
                String value = sp[1];
                String[] vs = StringUtils.split(value, " ");
                double[] v = Arrays.stream(vs).mapToDouble(Double::parseDouble).toArray();
                if (v.length == 200) {
                    map.put(count.intValue(), key);
                    count.addAndGet(1);
                    data.add(v);
                } else {
                    System.out.println(s);
                }
                return null;
            }
        });

//        IndexGraph indexGraph = new IndexGraph();
//        indexGraph.fromData(data);
//        indexGraph.setL(200);
//        indexGraph.setNd(data.size());
//        indexGraph.setS(10);
//        indexGraph.setR(100);
//        indexGraph.setIter(10);
//        indexGraph.setK(100);
//        indexGraph.setDistance(new CommonDistance());
//        indexGraph.build();
//        indexGraph.save("/Users/songyaheng/Downloads/graph.txt");

        final PrintWriter writer = new PrintWriter(new File("/Users/songyaheng/Downloads/res.txt"));
        readFileByLines("/Users/songyaheng/Downloads/graph.txt", new Process<String, String>() {
            @Override
            public String process(String s) {
                String[] sp = StringUtils.split(s, " ");
                String ss = StringUtils.join(Arrays.stream(sp).map(Integer::parseInt).map(map::get).collect(Collectors.toList()), ",");
                writer.write(ss + "\n");
                return null;
            }
        });
        writer.close();

//        NsgIndex nsgIndex = new NsgIndex(data);
//        nsgIndex.setDim(dim);
//        nsgIndex.setL(40);
//        nsgIndex.setR(50);
//        nsgIndex.setC(500);
//        nsgIndex.setDistance(new CommonDistance());
//        nsgIndex.build("/Users/songyaheng/Downloads/graph.txt");
//        nsgIndex.save("/Users/songyaheng/Downloads/nsg_graph.txt");

//        NsgIndex index = new NsgIndex(data);
//        index.setDistance(new CommonDistance());
//        index.load("/Users/songyaheng/Downloads/nsg_graph.txt");
//        List<Integer> res = index.search(v, 40);
//        System.out.println(JSON.toJSONString(res));
    }


    public static void readFileByLines(String fileName, Process<String, String> process) {
        File file = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                process.process(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
