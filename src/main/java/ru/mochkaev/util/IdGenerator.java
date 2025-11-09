package ru.mochkaev.util;

import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {
    private static final AtomicLong seq = new AtomicLong(1);
    private IdGenerator() {}
    public static long nextId() {
        return seq.getAndIncrement();
    }
}
