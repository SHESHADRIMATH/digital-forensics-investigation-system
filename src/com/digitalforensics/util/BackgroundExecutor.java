package com.digitalforensics.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * One shared thread pool for heavy work (hashing files, analysing logs).
 * Using a pool instead of "new Thread()" each time limits how many threads
 * run at once and re-uses them. Started and stopped by AppContextListener.
 */
public final class BackgroundExecutor {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(4, runnable -> {
        Thread t = new Thread(runnable, "forensics-worker");
        t.setDaemon(true);
        return t;
    });

    private BackgroundExecutor() {
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return POOL.submit(task);
    }

    public static void shutdown() {
        POOL.shutdown();
        try {
            if (!POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
