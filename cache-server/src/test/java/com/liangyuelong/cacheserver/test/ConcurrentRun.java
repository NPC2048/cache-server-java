package com.liangyuelong.cacheserver.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentRun {

    public static void run(int size, Runnable runnable){
        CountDownLatch latch = new CountDownLatch(size);
        CountDownLatch concurrentLatch = new CountDownLatch(1);
        ExecutorService service = Executors.newFixedThreadPool(size);
        for (int i = 0; i < size; i++) {
            service.execute(() -> {
                try {
                    concurrentLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    runnable.run();
                } finally {
                    latch.countDown();
                }
            });
        }
        concurrentLatch.countDown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
