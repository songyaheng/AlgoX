package com.github.algox.common;

import java.util.concurrent.Callable;

public abstract class Task implements Callable<Void> {
    @Override
    public Void call() throws Exception {
        run();
        return null;
    }

    protected abstract void run() throws Exception;
}
