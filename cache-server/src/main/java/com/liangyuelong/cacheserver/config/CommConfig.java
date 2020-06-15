package com.liangyuelong.cacheserver.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 公共配置
 *
 * @author yuelong.liang
 */
@ConfigurationProperties(prefix = "cache-server")
@Configuration
@Data
public class CommConfig implements InitializingBean {

    /**
     * hash 服务器地址
     */
    private String hashServerHost;


    @Override
    public void afterPropertiesSet() {
    }
}
