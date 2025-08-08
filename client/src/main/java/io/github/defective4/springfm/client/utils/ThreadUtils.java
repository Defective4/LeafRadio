package io.github.defective4.springfm.client.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtils {
    private static final ExecutorService SERVICE;

    static {
        SERVICE = Executors.newVirtualThreadPerTaskExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> SERVICE.shutdownNow()));
    }

    private ThreadUtils() {
    }

    public static Future<?> submit(Runnable run) {
        return SERVICE.submit(run);
    }
}
