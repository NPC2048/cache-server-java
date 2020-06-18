package com.liangyuelong.cacheserver.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 公共配置
 *
 * @author yuelong.liang
 */
@ConfigurationProperties(prefix = "cache-server")
@Configuration
@Data
@Slf4j
public class CommConfig implements InitializingBean {

    /**
     * hash 服务器地址
     */
    private String hashServerHost;

    @Value("${memcached.servers}")
    private String[] servers;


    @Override
    public void afterPropertiesSet() {
        log.info("--------- hash server 地址: " + hashServerHost);
        log.info("--------- memcached 地址: " + Arrays.toString(servers));
    }
}
