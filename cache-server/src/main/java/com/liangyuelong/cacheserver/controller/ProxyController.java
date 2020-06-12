package com.liangyuelong.cacheserver.controller;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.function.Consumer;

/**
 * 请求转发 controller
 */
@RestController
public class ProxyController {

    @RequestMapping("/")
    public Mono<Object> proxy() {

        return Mono.create(objectMonoSink -> {
        });
    }

}
