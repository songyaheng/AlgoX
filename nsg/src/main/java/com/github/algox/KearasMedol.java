package com.github.algox;

import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import sun.reflect.Reflection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class KearasMedol {
    ///Users/songyaheng/Downloads/opencv_java401.dll
    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // ComputationGraph network = KerasModelImport.importKerasModelAndWeights("/Users/songyaheng/Downloads/vgg16_modle_1.json", "/Users/songyaheng/Downloads/vgg16_model_2.h5");
//        NativeImageLoader loader = new NativeImageLoader(640, 640, 3);
//        INDArray image = loader.asMatrix(new File("/Users/songyaheng/Downloads/303/303.jpg"));
        Mat mat = Imgcodecs.imread("/Users/songyaheng/Downloads/303/303.jpg");
        double bm = 103.939;
        double gm = 116.779;
        double rm = 123.68;
//        INDArray b = image.get(NDArrayIndex.all(), NDArrayIndex.indices(0)).sub(bm);
//        INDArray g = image.get(NDArrayIndex.all(), NDArrayIndex.indices(1)).sub(gm);
//        INDArray r = image.get(NDArrayIndex.all(), NDArrayIndex.indices(2)).sub(rm);
//        System.out.println(r);
//        INDArray[] arrays = network.output(image);
//        System.out.println(arrays[0]);
    }
}
