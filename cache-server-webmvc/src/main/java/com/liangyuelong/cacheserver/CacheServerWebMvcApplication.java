package com.liangyuelong.cacheserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 主程序入口
 *
 * @author yuelong.liang
 */
@EnableConfigurationProperties
@SpringBootApplication
public class CacheServerWebMvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheServerWebMvcApplication.class, args);
    }

}
