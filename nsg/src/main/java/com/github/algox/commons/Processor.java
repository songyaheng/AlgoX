package com.github.algox.commons;

public interface Processor<I, O> {
    O process(I i);
}
