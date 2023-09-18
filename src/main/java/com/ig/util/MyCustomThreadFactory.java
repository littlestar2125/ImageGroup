package com.ig.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: zhi.li
 * @date: 2022/5/29 1:23
 */
public class MyCustomThreadFactory implements ThreadFactory {
    AtomicInteger threadCount = new AtomicInteger(0);
    private final String name;

    public MyCustomThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name + "-" + threadCount.incrementAndGet());
    }
}