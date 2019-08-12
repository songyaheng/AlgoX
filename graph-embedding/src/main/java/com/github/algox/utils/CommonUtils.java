package com.github.algox.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.*;
import java.util.List;

/**
 * @author songyaheng
 */
public class CommonUtils {

    private static final FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    public static List<String> loadDataFromResource(Class<?> clazz, String fileName) throws IOException {
        List<String> words = Lists.newArrayList();
        InputStream stream = clazz.getClassLoader().getResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String str = null;
        while((str = reader.readLine()) != null)
        {
            if (!StringUtils.isEmpty(str)) {
                words.add(str.trim());
            }
        }
        stream.close();
        reader.close();
        return words;
    }

    public static List<String> loadWordEmbeddingData(String path) throws IOException {
        List<String> words = Lists.newArrayList();
        InputStream stream = new FileInputStream(new File(path));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String str = null;
        while((str = reader.readLine()) != null)
        {
            if (!StringUtils.isEmpty(str)) {
                words.add(str.trim());
            }
        }
        stream.close();
        reader.close();
        return words;
    }

    public static double sum(List<Double> list) {
        double sum = 0;
        for (double a : list) {
            sum += a;
        }
        return sum;
    }

    public static double sum(double[] array) {
        double sum = 0;
        for (double a : array) {
            sum += a;
        }
        return sum;
    }

    public static double[] probs(double[] array) {
        double[] probs = new double[array.length];
        double sum = sum(array);
        int i = 0;
        for (double v : array) {
            probs[i] = v / sum;
            i ++;
        }
        return probs;
    }

    public static String getNow() {
        return format.format(System.currentTimeMillis());
    }

    public static boolean isEven(int i) {
        return (i&1)==0;
    }

    public static boolean isOdd(int i) {
        return !isEven(i);
    }

    public static <X extends Comparable<? super X>> int compare(X x1, X x2) {
        if (x1 == null) {
            return x2 == null ? 0 : -1;
        }
        return x2 == null ? 1 : x1.compareTo(x2);
    }
}
