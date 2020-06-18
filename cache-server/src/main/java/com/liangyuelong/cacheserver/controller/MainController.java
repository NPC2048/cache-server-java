package com.liangyuelong.cacheserver.controller;

import com.liangyuelong.cacheserver.common.util.MemClientUtils;
import com.liangyuelong.cacheserver.hash.HashServerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.function.Consumer;

/**
 * 主要 controller
 *
 * @author yuelong.liang
 */
@RestController
@Slf4j
public class MainController {


    /**
     * 从 hash server 或 memcached 获取密钥 hash
     * 如果有 input, 则进行 hash 缓存
     * 如果为其他的, 则只是转发处理
     *
     * @param path    请求路径
     * @param request webflux request
     * @param input   input
     * @param body    request body
     * @return Mono<String>
     */
    @RequestMapping("/{path}")
    public Mono<String> calc(@PathVariable String path, ServerHttpRequest request, String input, @RequestBody(required = false) String body) {
        log.info("========== begin request :" + request.getId() + ", input: " + input);
        // 如果 input 为空，获取转发的值后返回
        if (StringUtils.isEmpty(input)) {
            return Mono.just(HashServerUtils.request(request, path, body));
        }
        Consumer<MonoSink<String>> consumer = sink -> {
            MemClientUtils.getHash(request, path, body, sink, input);
        };
        return Mono.create(consumer);
    }

}
