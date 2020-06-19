package com.liangyuelong.cacheserver.config;

import com.whalin.MemCached.MemCachedClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.Resource;

@EnableWebMvc
@Configuration
@Slf4j
public class WebConfig implements InitializingBean {

    @Resource
    private MemCachedClient memCachedClient;

    @Resource
    private CommConfig commConfig;

    @Override
    public void afterPropertiesSet() {
        log.info("hash server 地址:" + commConfig.getHashServerHost());
    }

}
