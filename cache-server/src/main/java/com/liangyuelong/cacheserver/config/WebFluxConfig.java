package com.liangyuelong.cacheserver.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.WebHandler;

@Configuration
@EnableWebFlux
public class WebFluxConfig {

    @Bean
    public WebHandler webHandler(ApplicationContext context) {
        return new DispatcherHandler(context);
    }

}
