/*
package com.teamnest.gateway.filters;

import org.slf4j.MDC;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
public class CorrelationIdFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Bean
    public GlobalFilter correlationIdGlobalFilter() {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            var headers = request.getHeaders();

            String correlationId = firstNonEmpty(
                headers.getFirst(CORRELATION_ID_HEADER),
                headers.getFirst(REQUEST_ID_HEADER),
                UUID.randomUUID().toString()
            );

            // propagate in MDC & request attributes
            MDC.put("correlationId", correlationId);

            var mutated = exchange.mutate().request(builder ->
                builder.header(CORRELATION_ID_HEADER, correlationId)
                    .header(REQUEST_ID_HEADER, correlationId)
            ).build();

            return chain.filter(mutated)
                .doFinally(sig -> MDC.remove("correlationId"));
        };
    }

    private static String firstNonEmpty(String... vals) {
        for (var s : vals) if (s != null && !s.isBlank()) return s;
        return null;
    }
}
*/
