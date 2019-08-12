package com.github.algox.huffman;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import java.util.ArrayList;
import java.util.Map;

public class HuffmanTree {
    private ImmutableMultiset<String> vocab;

    public HuffmanTree(ImmutableMultiset<String> vocab) {
        this.vocab = vocab;
    }

    public Map<String, HuffmanNode> encode() {
        final int numTokens = vocab.elementSet().size();
        int[] parentNode = new int[numTokens * 2 + 1];
        byte[] binary = new byte[numTokens * 2 + 1];
        long[] count = new long[numTokens * 2 + 1];
        int i = 0;
        for (Multiset.Entry<String> e : vocab.entrySet()) {
            count[i] = e.getCount();
            i++;
        }
        Preconditions.checkState(i == numTokens, "Expected %s to match %s", i, numTokens);
        for (i = numTokens; i < count.length; i++) {
            count[i] = (long)1e15;
        }
        createTree(numTokens, count, binary, parentNode);
        return encode(binary, parentNode);
    }

    private void createTree(int numTokens, long[] count, byte[] binary, int[] parentNode) {
        int min1i;
        int min2i;
        int pos1 = numTokens - 1;
        int pos2 = numTokens;
        for (int a = 0; a < numTokens - 1; a++) {
            if (pos1 >= 0) {
                if (count[pos1] < count[pos2]) {
                    min1i = pos1;
                    pos1--;
                } else {
                    min1i = pos2;
                    pos2++;
                }
            } else {
                min1i = pos2;
                pos2++;
            }
            if (pos1 >= 0) {
                if (count[pos1] < count[pos2]) {
                    min2i = pos1;
                    pos1--;
                } else {
                    min2i = pos2;
                    pos2++;
                }
            } else {
                min2i = pos2;
                pos2++;
            }
            int newNodeIdx = numTokens + a;
            count[newNodeIdx] = count[min1i] + count[min2i];
            parentNode[min1i] = newNodeIdx;
            parentNode[min2i] = newNodeIdx;
            binary[min2i] = 1;
        }
    }

    private Map<String, HuffmanNode> encode(byte[] binary, int[] parentNode) {
        int numTokens = vocab.elementSet().size();
        ImmutableMap.Builder<String, HuffmanNode> result = ImmutableMap.builder();
        int nodeIdx = 0;
        for (Multiset.Entry<String> e : vocab.entrySet()) {
            int curNodeIdx = nodeIdx;
            ArrayList<Byte> code = new ArrayList<>();
            ArrayList<Integer> points = new ArrayList<>();
            while (true) {
                code.add(binary[curNodeIdx]);
                points.add(curNodeIdx);
                curNodeIdx = parentNode[curNodeIdx];
                if (curNodeIdx == numTokens * 2 - 2) {
                    break;
                }
            }
            int codeLen = code.size();
            final int count = e.getCount();
            final byte[] rawCode = new byte[codeLen];
            final int[] rawPoints = new int[codeLen + 1];

            rawPoints[0] = numTokens - 2;
            for (int i = 0; i < codeLen; i++) {
                rawCode[codeLen - i - 1] = code.get(i);
                rawPoints[codeLen - i] = points.get(i) - numTokens;
            }
            String token = e.getElement();
            result.put(token, new HuffmanNode(rawCode, rawPoints, nodeIdx, count));
            nodeIdx++;
        }
        return result.build();
    }
}
