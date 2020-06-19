package com.liangyuelong.cacheserver.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 从 server 获取 hash 线程池配置
 *
 * @author yuelong.liang
 */
@Configuration
@ConfigurationProperties(prefix = "hash.pool")
@Data
@Slf4j
public class HashGetPoolConfig {

    /**
     * 核心线程数
     */
    private int coreSize;

    /**
     * 空闲线程所允许的空闲时间, 默认为 60 s
     */
    private int keepAliveSeconds;

    /**
     * 最大线程数
     */
    private int maxPoolSize;

    /**
     * 队列长度
     */
    private int queueCapacity;

    /**
     * http 从 server 获取 hash 线程池
     */
    @Bean
    public ThreadPoolTaskExecutor hashGetThreadPoolTaskExecutor() {
        log.info("---------- 获取 hash 线程池配置 begin ----------");
        log.info("coreSize: " + coreSize);
        log.info("keepAliveSeconds: " + keepAliveSeconds);
        log.info("maxPoolSize: " + maxPoolSize);
        log.info("queueCapacity: " + queueCapacity);
        log.info("---------- 获取 hash 线程池配置 end ----------");
        ThreadPoolTaskExecutor hashGetThreadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 核心线程数, 默认 1
        hashGetThreadPoolTaskExecutor.setCorePoolSize(coreSize);
        // 空闲线程所允许的空闲时间, 默认为 60s
        hashGetThreadPoolTaskExecutor.setKeepAliveSeconds(Math.abs(keepAliveSeconds));
        // 最大线程数, 默认 2 ^ 64 - 1
        hashGetThreadPoolTaskExecutor.setMaxPoolSize(Integer.MAX_VALUE);
        // 队列长度, 默认同上
        hashGetThreadPoolTaskExecutor.setQueueCapacity(Integer.MAX_VALUE);
        // 线程池已满后，对后续任务的策略: CallerRunPolicy, 由调用线程自己处理回该任务
        hashGetThreadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return hashGetThreadPoolTaskExecutor;
    }
}
