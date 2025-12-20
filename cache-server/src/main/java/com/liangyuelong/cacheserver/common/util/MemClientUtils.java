package com.liangyuelong.cacheserver.common.util;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liangyuelong.cacheserver.util.HashServerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
     * 以 input 的 md5 作为 key
     */
    private static final Map<String, Mono<String>> IN_FLIGHT = new ConcurrentHashMap<>(256);

    private static final Cache<String, String> CACHE = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build();

    /**
     * 从 server 获取 hash 或从 CACHE 获取缓存 hash
     * 1.进入该方法时，对 input 进行 md5 取值, 将该 md5 值作为 key
     * 2.根据 key 从 CACHE 获取值
     * 3.判断是否有值, 如果没有判断是否有相同的 input, 如果没有则创建 MONO 后，开始从 server 请求 hash
     * 4.后续相同 input 获取第一个mono的结果返回
     * 5.从 server 获取 hash 的线程执行完后, 将 key 对应的值存入 memcached, 并将 key 从 map 删除,
     *
     * @param request     webflux request
     * @param path        请求路径
     * @param requestBody 请求 body
     * @param input       密钥
     */
    public static Mono<String> getHash(ServerHttpRequest request, String path, String requestBody, String input) {
        String key = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        // 1. 命中本地缓存，直接返回
        String cached = CACHE.getIfPresent(key);
        if (cached != null) {
            return Mono.just(cached);
        }
        // 2. single-flight：相同 key 只创建一次 Mono
        return IN_FLIGHT.computeIfAbsent(key, k -> createHashMono(request, path, requestBody, key));
    }

    /**
     * 真正执行远端请求的 Mono
     */
    private static Mono<String> createHashMono(ServerHttpRequest request, String path, String requestBody, String key) {
        Callable<String> callable = () -> {
            String response = HashServerUtils.request(path, request.getMethod().name(),
                    request.getHeaders().toSingleValueMap(), request.getQueryParams().toSingleValueMap(),
                    StringUtils.defaultIfEmpty(requestBody, "").getBytes(StandardCharsets.UTF_8));
            if (!HashServerUtils.isSuccess(response)) {
                throw new IllegalStateException("Hash server response invalid");
            }
            return response;
        };
        return Mono.fromCallable(callable)
                // 3. 阻塞 IO → 弹性线程池
                .subscribeOn(Schedulers.boundedElastic())
                // 4. 重试策略（替代 while + sleep）
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
                        .doBeforeRetry(rs -> log.warn("Retry get hash, attempt={}", rs.totalRetries() + 1)))
                // 5. 成功后写缓存
                .doOnSuccess(hash -> {
                    CACHE.put(key, hash);
                    log.info("hash cached, key={}", key);
                })
                // 6. 无论成功 / 失败 / cancel，都清理占位
                .doFinally(signalType -> {
                    IN_FLIGHT.remove(key);
                    log.debug("in-flight cleared, key={}, signal={}", key, signalType);
                })
                // 7. 关键：结果复用，确保只执行一次
                .cache();
    }


}
