package com.teamnest.gateway.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaffeineConfig {
    @Bean(name = "staleResponses")
    public Cache<String, byte[]> staleResponses() {
        return com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(java.time.Duration.ofSeconds(60))
            .build();
    }

}
