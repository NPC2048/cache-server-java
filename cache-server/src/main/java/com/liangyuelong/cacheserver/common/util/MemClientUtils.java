package com.liangyuelong.cacheserver.common.util;


import com.liangyuelong.cacheserver.util.HashServerUtils;
import com.whalin.MemCached.MemCachedClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.MonoSink;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * memcached 操作工具类
 * 目前用于同步更新 key 的情况
 *
 * @author yuelong.liang
 */
@Slf4j
@Component
public class MemClientUtils {

    /**
     * memcached 客户端工具
     */
    private static MemCachedClient client;

    /**
     * 从 hash server 获取 hash 的线程池
     */
    private static ThreadPoolTaskExecutor hashGetThreadPoolTaskExecutor;

    /**
     * 以 input 的 md5 作为 key
     * 相同的 input 都加入 key 响应的 queue 中
     */
    private static Map<String, Queue<MonoSink<String>>> map = new ConcurrentHashMap<>(256);

    @Resource
    public void setClient(MemCachedClient client) {
        MemClientUtils.client = client;
    }

    @Resource
    public void setHashGetThreadPoolTaskExecutor(ThreadPoolTaskExecutor hashGetThreadPoolTaskExecutor) {
        MemClientUtils.hashGetThreadPoolTaskExecutor = hashGetThreadPoolTaskExecutor;
    }

    /**
     * 从 server 获取 hash 或从 memcached 获取缓存 hash
     * 1.进入该方法时，对 input 进行 md5 取值, 将该 md5 值作为 key
     * 2.根据 key 从 memcached 获取值
     * 3.判断是否有值, 如果没有判断是否有相同的 input, 如果没有则创建 queue 后，开始从 server 请求 hash
     * 4.后续相同 input 各自进入相对应的 queue, 等待第一个线程获取 hash 后, 通知自己返回 hash
     * 5.从 server 获取 hash 的线程执行完后, 将 key 对应的值存入 memcached, 并将 key 从 map 删除,
     * 之后, 遍历 MonoSink 队列, 通知其他 reqeust 返回 hash
     *
     * @param request     webflux request
     * @param path        请求路径
     * @param requestBody 请求 body
     * @param sink        MonoSink
     * @param input       密钥
     */
    public static void getHash(ServerHttpRequest request, String path, String requestBody, MonoSink<String> sink, String input) {
        String key = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        String hash = (String) client.get(key);
        // 判断 hash 是否已存在, 如果已存在, 直接返回
        if (hash != null) {
            sink.success(hash);
            return;
        }
        // 判断是否已有正在处理中的队列
        Queue<MonoSink<String>> queue = map.get(key);
        if (queue == null) {
//            log.info("第一个:" + input);
            queue = new ConcurrentLinkedQueue<>();
            map.put(key, queue);
            final Queue<MonoSink<String>> finalQueue = queue;
            // 启用线程去 server 获取 hash
            hashGetThreadPoolTaskExecutor.execute(() -> {
//                log.info("新线程: " + Thread.currentThread());
                String getHash;
                do {
                    getHash = HashServerUtils.request(path, request.getMethodValue(), request.getHeaders().toSingleValueMap(),
                            request.getQueryParams().toSingleValueMap(), StringUtils.getBytes(requestBody, StandardCharsets.UTF_8));
                    log.info("hash:" + getHash);
                    log.info("success:" + HashServerUtils.isSuccess(getHash));
                } while (!HashServerUtils.isSuccess(getHash));
                // 存入 mem
                client.set(key, getHash);
                // 从 map 删除
                map.remove(key);
                // 返回
                sink.success(getHash);
                // 通知其他 quque
//                log.info("返回:" + finalQueue.size());
                for (MonoSink<String> monoSink : finalQueue) {
                    monoSink.success(getHash);
                }
            });
        } else {
//            log.info("后续:" + input);
            // 加入 queue
            queue.add(sink);
        }
//        log.info("map 数量:" + map.size());
    }

    /**
     * 以 input 的 md5 作为 key
     * 相同的 input 都加入 key 响应的 queue 中
     */
    private static Map<String, Queue<FluxSink<String>>> FLUX_MAP = new ConcurrentHashMap<>(256);

    public static void getHash(ServerHttpRequest request, String path, String requestBody, FluxSink<String> sink, String input) {
        String key = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        String hash = (String) client.get(key);
        // 判断 hash 是否已存在, 如果已存在, 直接返回
        if (hash != null) {
            sink.next(hash);
            sink.complete();
            return;
        }
        // 判断是否已有正在处理中的队列
        Queue<FluxSink<String>> queue = FLUX_MAP.get(key);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<>();
            FLUX_MAP.put(key, queue);
            final Queue<FluxSink<String>> finalQueue = queue;
            // 启用线程去 server 获取 hash
            hashGetThreadPoolTaskExecutor.execute(() -> {
                String getHash;
                while (true) {
                    try {
                        getHash = HashServerUtils.request(path, request.getMethodValue(), request.getHeaders().toSingleValueMap(),
                                request.getQueryParams().toSingleValueMap(), StringUtils.getBytes(requestBody, StandardCharsets.UTF_8));
                        log.info("hash:" + getHash);
                        log.info("success:" + HashServerUtils.isSuccess(getHash));
                        if (HashServerUtils.isSuccess(getHash)) {
                            break;
                        }
                    } catch (Exception e) {
                        log.error(request.getId() + ":" + input + ":" + e.getMessage());
                    }
                }
                // 存入 mem
                client.set(key, getHash);
                // 从 map 删除
                FLUX_MAP.remove(key);
                // 返回
                sink.next(getHash);
                sink.complete();
                // 通知其他 quque
//                log.info("返回:" + finalQueue.size());
                for (FluxSink<String> fluxSink : finalQueue) {
                    fluxSink.next(getHash);
                    fluxSink.complete();
                }
            });
        } else {
            // 加入 queue
            queue.add(sink);
        }
    }

}
