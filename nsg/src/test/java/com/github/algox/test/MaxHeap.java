package com.github.algox.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MaxHeap<E extends Comparable<E>> {
    //这里使用数组来实现
    private ArrayList<E> data;

    public MaxHeap(int capacity) {
        data = new ArrayList<>(capacity);
    }

    public MaxHeap() {
        data = new ArrayList<>();
    }

    //返回堆中元素的个数
    public int getSize() {
        return data.size();
    }

    //判断堆是否为空
    public boolean isEmpty() {
        return data.isEmpty();
    }

    //返回完全二叉树中索引为index的节点的父节点的索引
    private int parent(int index) {
        if (index == 0)
            throw new IllegalArgumentException("index-0 doesn't have parent");

        return (index - 1) / 2;
    }

    //返回完全二叉树中索引为index的节点的左孩子的索引
    private int leftChild(int index) {
        return index * 2 + 1;
    }

    //返回完全二叉树中索引为index的节点的右孩子的索引
    private int rightChild(int index) {
        return index * 2 + 2;
    }

    //交换索引为i、j的值
    private void swap(int i, int j) {
        E t = data.get(i);
        data.set(i, data.get(j));
        data.set(j, t);
    }

    //向堆中添加元素
    public void add(E e) {
        data.add(e);
        siftUp(data.size() - 1);
    }

    private void siftUp(int k) {
        //k不能是根节点，并且k的值要比父节点的值大
        while (k > 0 && data.get(parent(k)).compareTo(data.get(k)) < 0) {
            swap(k, parent(k));
            k = parent(k);
        }
    }

    //看堆中最大的元素
    public E findMax() {
        if (data.size() == 0)
            throw new IllegalArgumentException("Can not findMax when heap is empty");
        return data.get(0);
    }

    //取出堆中最大的元素
    public E extractMax() {
        E ret = findMax();

        swap(0, data.size() - 1);
        data.remove(data.size() - 1);
        siftDown(0);

        return ret;
    }

    private void siftDown(int k) {
        //leftChild存在
        while (leftChild(k) < data.size()) {
            int j = leftChild(k);
            //rightChild存在,且值大于leftChild的值
            if (j + 1 < data.size() &&
                    data.get(j).compareTo(data.get(j + 1)) < 0)
                j = rightChild(k);
            //data[j]是leftChild和rightChild中最大的

            if (data.get(k).compareTo(data.get(j)) >= 0)
                break;

            swap(k, j);
            k = j;
        }

    }

    //取出堆中最大的元素,替换为元素e
    public E replace(E e){
        E ret = findMax();
        data.set(0, e);
        siftDown(0);
        return ret;
    }

    //heapify操作:将数组转化为堆
    public MaxHeap(E[] arrs) {
        data = new ArrayList<>(Arrays.asList(arrs));
        for (int i = parent(data.size() - 1); i >= 0; i--) {
            siftDown(i);
        }
    }
    public static void main(String[] args) {
        MaxHeap<Integer> maxHeap = new MaxHeap<>();
        maxHeap.add(1);
        maxHeap.add(2);
        maxHeap.add(3);
        maxHeap.add(4);
        maxHeap.add(5);
        System.out.println(maxHeap.data);
    }
}
