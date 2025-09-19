package com.teamnest.gateway.trace;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Hooks;

@Configuration
public class RequestIdFilterConfig {

    public static final String HEADER = "X-Request-Id";
    public static final String CONTEXT_KEY = "requestId";



    @PostConstruct
    public void enableCtxPropagationOnce() {
        Hooks.enableAutomaticContextPropagation();
    }

    @Bean
    public WebFilter requestIdFilter() {
        return (exchange, chain) -> {
            String id = exchange.getRequest().getHeaders().getFirst(HEADER);
            if (id == null || id.isBlank()) {
                id = UUID.randomUUID().toString();
            }
            exchange.getResponse().getHeaders().addIfAbsent(HEADER, id);
            exchange.getAttributes().put(CONTEXT_KEY, id);
            final String rid = id;
            return chain.filter(exchange).contextWrite(ctx -> ctx.put(CONTEXT_KEY, rid));
        };
    }
}