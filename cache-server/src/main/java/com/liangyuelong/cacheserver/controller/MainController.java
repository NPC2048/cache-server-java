package com.liangyuelong.cacheserver.controller;

import com.liangyuelong.cacheserver.constants.CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
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
@CacheConfig(cacheNames = "hash-cache")
public class MainController {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Value("${cache-server.hash-server-host}")
    private String hashServerHost;

    @RequestMapping("/calc")
    public Mono<String> calc(String input) {
        // 判断缓存中是否存在
        // 对 input 进行 md5 取值
        String md5 = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        String key = CacheConstant.HASH_PREFIX + md5;
        // 从 redis 获取
        String hash = redisTemplate.opsForValue().get(key);
        // hash 不为空，直接返回
        if (hash != null) {
//            log.info("缓存命中: " + key);
            return Mono.just(hash);
        }
        // 从 hash server 获取值
        hash = restTemplate.getForObject(hashServerHost, String.class, "input", input);
        // 缓存至 redis
        redisTemplate.opsForValue().set(key, hash);
        return Mono.just(hash);
    }

}
