package com.liangyuelong.cacheserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SpeedController {

    @RequestMapping("/speed")
    public Mono<String> speed() {
        return Mono.just("hello world");
    }

}
