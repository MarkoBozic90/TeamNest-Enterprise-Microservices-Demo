package com.teamnest.gateway.test.unit;


import static com.teamnest.gateway.constant.StringConstant.CID_1;
import static com.teamnest.gateway.constant.StringConstant.CORRELATION_ID;
import static com.teamnest.gateway.constant.StringConstant.PATH_X;
import static com.teamnest.gateway.constant.StringConstant.PATH_Y;
import static org.assertj.core.api.Assertions.assertThat;

import com.teamnest.gateway.filters.CorrelationIdFilter;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class CorrelationIdFilterTest {

    private static final class CapturingGatewayChain implements GatewayFilterChain {
        final AtomicReference<ServerWebExchange> seen = new AtomicReference<>();
        @Override public Mono<Void> filter(ServerWebExchange exchange) {
            assertThat(MDC.get(CORRELATION_ID)).isNotBlank();
            seen.set(exchange);
            return Mono.empty();
        }
    }

    @Test
    void preserves_existing_and_sets_both_then_cleans_mdc() {
        GlobalFilter gf = new CorrelationIdFilter().correlationIdGlobalFilter();

        var ex = MockServerWebExchange.from(
            MockServerHttpRequest.get(PATH_X)
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, CID_1)
                .build()
        );
        var chain = new CapturingGatewayChain();

        gf.filter(ex, chain).block();

        var mutated = chain.seen.get();
        assertThat(mutated.getRequest().getHeaders()
            .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(CID_1);
        assertThat(mutated.getRequest().getHeaders()
            .getFirst(CorrelationIdFilter.REQUEST_ID_HEADER)).isEqualTo(CID_1);
        // posle izvršenja, MDC je očišćen
        assertThat(MDC.get(CORRELATION_ID)).isNull();
    }

    @Test
    void generates_when_missing_and_sets_both_then_cleans_mdc() {
        GlobalFilter gf = new CorrelationIdFilter().correlationIdGlobalFilter();

        var ex = MockServerWebExchange.from(MockServerHttpRequest.get(PATH_Y).build());
        var chain = new CapturingGatewayChain();

        gf.filter(ex, chain).block();

        var mutated = chain.seen.get();
        var cid = mutated.getRequest().getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
        var rid = mutated.getRequest().getHeaders().getFirst(CorrelationIdFilter.REQUEST_ID_HEADER);

        assertThat(cid).isNotBlank();
        assertThat(rid).isEqualTo(cid);
        assertThat(MDC.get(CORRELATION_ID)).isNull();
    }
}