package com.teamnest.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@TestConfiguration(proxyBeanMethods = false)
public class TestRedisConfig {

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(Environment env) {
        String host = env.getRequiredProperty("spring.data.redis.host");
        int port = Integer.parseInt(env.getRequiredProperty("spring.data.redis.port"));
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveStringRedisTemplate(factory);
    }
}
