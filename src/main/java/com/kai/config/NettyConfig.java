package com.kai.config;

import com.kai.server.NettyServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PostConstruct;

@Configuration
public class NettyConfig {


    private final ApplicationContext applicationContext;

    public NettyConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public NettyServer nettyServer() {
        return new NettyServer(applicationContext); // 传递 Spring 容器
    }


}
