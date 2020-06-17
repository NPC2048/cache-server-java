package com.liangyuelong.cacheserver.common.util;


import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

/**
 * memcached 操作工具类
 * 目前用于同步更新 key 的情况
 *
 * @author yuelong.liang
 */
public class MemClientUtils {

    public static void setToProcessing(Flux<String> flux, String key) {
        // 观察则队列
        ArrayBlockingQueue<FluxSink<String>> fluxes = new ArrayBlockingQueue<>(10);
        Consumer<FluxSink<String>> fluxSink = value -> {
            value.next(key);
            value.complete();
        };
        flux.create(fluxSink);
    }

    /**
     * 将 value 通知给 queue 里的所有 FluxShik 队列
     *
     * @param queue FluxSink 队列
     * @param value 值
     */
    public static void notify(Queue<FluxSink<String>> queue, String value) {
        if (!queue.isEmpty()) {
            for (FluxSink<String> fluxSink : queue) {
                fluxSink.next(value);
                fluxSink.complete();
            }
        }
    }

}
