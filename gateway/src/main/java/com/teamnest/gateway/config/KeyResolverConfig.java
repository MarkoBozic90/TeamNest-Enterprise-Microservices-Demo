package com.teamnest.gateway.config;

import static org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver.X_FORWARDED_FOR;
import static org.springframework.security.config.Elements.ANONYMOUS;

import java.security.Principal;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/** Resolves rate-limit key by principal if present, else by IP. */
@Configuration
public class KeyResolverConfig {

    @Bean("principalOrIpKeyResolver")
    public KeyResolver principalOrIpKeyResolver() {
        return exchange -> exchange.getPrincipal()
            .map(Principal::getName)
            .switchIfEmpty(Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst(X_FORWARDED_FOR)))
            .switchIfEmpty(Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : ANONYMOUS));
    }


}
