package com.liangyuelong.cacheserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yuelong.liang
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class MockHashServer {


    /**
     * 每秒最多5个请求
     */
    private static final int MAX_QPS = 5;

    private static final AtomicLong CURRENT_SECOND = new AtomicLong(0);
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @RequestMapping("/mock/{path}")
    public Mono<ResponseEntity<String>> hash(@RequestParam("input") String input, @PathVariable String path) {
        log.info("path:{}", path);
        long nowSecond = Instant.now().getEpochSecond();
        long prevSecond = CURRENT_SECOND.get();

        // 时间窗口推进
        if (prevSecond != nowSecond) {
            if (CURRENT_SECOND.compareAndSet(prevSecond, nowSecond)) {
                COUNTER.set(0);
            }
        }

        // 限流判断
        if (COUNTER.incrementAndGet() > MAX_QPS) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Too busy. Service unavailable."));
        }

        // 计算 hash（同步、快速）
        String hash = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        return Mono.delay(Duration.ofSeconds(1L)).map(_ -> ResponseEntity.ok(hash));
    }

}
