package com.liangyuelong.cacheserver.config;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * memcached 配置
 *
 * @author yuelong.liang
 */
@Configuration
@ConfigurationProperties(prefix = "memcached")
@Data
public class MemcachedConfig {

    /**
     * 服务器地址列表
     */
    private String[] servers;

    private boolean failover;

    private int initConn;

    private int minConn;

    private int maxConn;

    private int maintSleep;

    private boolean nagel;

    private int socketTO;

    private boolean aliveCheck;

    @Bean
    public SockIOPool sockIOPool() {
        SockIOPool pool = SockIOPool.getInstance();
        pool.setServers(servers);
        pool.setFailover(failover);
        pool.setInitConn(initConn);
        pool.setMinConn(minConn);
        pool.setMaxConn(maxConn);
        pool.setMaintSleep(maintSleep);
        pool.setNagle(nagel);
        pool.setSocketTO(socketTO);
        pool.setAliveCheck(aliveCheck);
        pool.initialize();
        return pool;
    }

    @Bean
    public MemCachedClient memCachedClient() {
        return new MemCachedClient();

    }
}
