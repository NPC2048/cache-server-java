package com.liangyuelong.cacheserver.controller;

import com.liangyuelong.cacheserver.hash.HashServerUtils;
import com.whalin.MemCached.MemCachedClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * 主要 controller
 *
 * @author yuelong.liang
 */
@RestController
@Slf4j
public class MainController {

    @Resource
    private MemCachedClient client;

    @Value("${cache-server.hash-server-host}")
    private String hashServerHost;

    @RequestMapping("/{path}")
    public Mono<?> calc(@PathVariable String path, ServerHttpRequest request, String input, @RequestBody(required = false) String body) {
        log.info("target path: " + path);
        log.info("body: " + body);
        log.info("input: " + input);
        log.info(request.toString());
        // 对 input 进行 md5 取值
        String key = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        // 从 redis 获取
        String hash = (String) client.get(key);
//        String hash = redisTemplate.opsForValue().get(key);
        // hash 不为空，直接返回
        if (hash != null) {
//            log.info("缓存命中: " + key);
            return Mono.just(hash);
        }
        // 加入订阅者队列

        // 从 hash server 获取值
        do {
            hash = HashServerUtils.request(request, path, body);
            // 判断是否正确
        } while (!HashServerUtils.isSuccess(hash));
        // 缓存至 redis
        client.set(key, hash);
        return Mono.just(hash);
    }
}
