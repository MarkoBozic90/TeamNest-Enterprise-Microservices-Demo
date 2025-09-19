package com.teamnest.gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalancerCacheConfig {
    @Bean
    public CacheManager loadBalancerCacheManager() {
        var cm = new CaffeineCacheManager();
        cm.setCaffeine(Caffeine.newBuilder().maximumSize(1000));
        return cm;
    }
}
