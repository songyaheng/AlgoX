package com.github.algox.huffman;

public class HuffmanNode {
    public byte[] code;
    public int[] point;
    public int index;
    public int count;

    public HuffmanNode(byte[] code, int[] point, int index, int count) {
        this.code = code;
        this.point = point;
        this.index = index;
        this.count = count;
    }
}
