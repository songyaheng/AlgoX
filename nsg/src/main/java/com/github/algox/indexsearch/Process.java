package com.github.algox.indexsearch;

public interface Process<T, R> {
    R process(T t);
}
