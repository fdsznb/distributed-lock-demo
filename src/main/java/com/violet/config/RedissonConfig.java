package com.violet.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        // 可以用 "rediss://" 来启用SSL连接
        config.useSingleServer().setAddress("redis://localhost:5555").setPassword("violet2021");
        return Redisson.create(config);
    }
}