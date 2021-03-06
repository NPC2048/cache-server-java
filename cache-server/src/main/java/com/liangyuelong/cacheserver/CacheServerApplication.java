package com.liangyuelong.cacheserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class CacheServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheServerApplication.class, args);
    }

}
