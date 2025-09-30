package com.teamnest.gateway.config;

import java.security.Principal;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/** Resolves rate-limit key by principal if present, else by IP. */
@Configuration
public class KeyResolverConfig {
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String ANONYMOUS = "anonymous";

    @Bean("principalOrIpKeyResolver")
    public KeyResolver principalOrIpKeyResolver() {
        return exchange -> exchange.getPrincipal()
            .map(Principal::getName)
            .switchIfEmpty(Mono.justOrEmpty(
                    exchange.getRequest().getHeaders().getFirst(X_FORWARDED_FOR))
                .map(xff -> xff.contains(",") ? xff.substring(0, xff.indexOf(',')).trim() : xff)
            )
            .switchIfEmpty(Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : ANONYMOUS));
    }

}