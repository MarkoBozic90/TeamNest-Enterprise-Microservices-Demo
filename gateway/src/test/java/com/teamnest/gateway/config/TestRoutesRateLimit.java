package com.teamnest.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRoutesRateLimit {
    @Bean RedisRateLimiter testRedisRateLimiter() { return new RedisRateLimiter(1, 1); }

    @Bean

    RouteLocator rlRoutes(RouteLocatorBuilder rlb,
                          KeyResolverConfig resolvers,
                          RedisRateLimiter testRedisRateLimiter) {
        return rlb.routes()
            .route("rl-basic", r -> r
                .path("/rl/test/**")
                .filters(f -> f.requestRateLimiter(c -> {
                    c.setKeyResolver(resolvers.principalOrIpKeyResolver());
                    c.setRateLimiter(testRedisRateLimiter);
                }))
                .uri("forward:/__stub/flaky-twice-then-ok"))
            .build();
    }
}