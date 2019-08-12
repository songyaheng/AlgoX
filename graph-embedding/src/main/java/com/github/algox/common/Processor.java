package com.github.algox.common;

public interface Processor<T, R> {
    R process(T t);
}
