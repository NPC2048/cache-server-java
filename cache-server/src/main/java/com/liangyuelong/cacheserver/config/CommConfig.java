package com.liangyuelong.cacheserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 公共配置
 *
 * @author yuelong.liang
 */
@ConfigurationProperties(prefix = "cache-server")
@Configuration
@Data
public class CommConfig {

    /**
     * hash 服务器地址
     */
    private String hashServerHost;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
