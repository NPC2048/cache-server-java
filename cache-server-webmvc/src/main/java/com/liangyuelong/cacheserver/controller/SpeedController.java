package com.liangyuelong.cacheserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpeedController {

    @RequestMapping("/speed")
    public String speed() {
        return "hello world";
    }

}
