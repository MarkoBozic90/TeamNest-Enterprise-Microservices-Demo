package com.teamnest.gateway.test.unit;

import static com.teamnest.gateway.constant.StringConstant.GIVEN_1;
import static com.teamnest.gateway.constant.StringConstant.PATH_P;
import static org.assertj.core.api.Assertions.assertThat;

import com.teamnest.gateway.trace.RequestIdFilterConfig;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.WebFilter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RequestIdFilterConfigTest {

    @Test
    void generates_when_missing_and_propagates_to_headers_attributes_context() {
        var cfg = new RequestIdFilterConfig();
        cfg.enableCtxPropagationOnce(); // @PostConstruct (manually)

        WebFilter f = cfg.requestIdFilter();

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get(PATH_P).build());

        StepVerifier.create(
            f.filter(exchange, ex ->
                // provera Reactor context-a
                Mono.deferContextual(ctx -> {
                    String rid = ctx.get(RequestIdFilterConfig.CONTEXT_KEY);
                    assertThat(rid).isNotBlank();
                    return Mono.empty();
                })
            )
        ).verifyComplete();

        var headerRid = exchange.getResponse().getHeaders().getFirst(RequestIdFilterConfig.HEADER);
        var attrRid = (String) exchange.getAttributes().get(RequestIdFilterConfig.CONTEXT_KEY);

        assertThat(headerRid).isNotBlank();
        assertThat(attrRid).isEqualTo(headerRid);
    }

    @Test
    void uses_existing_request_id_when_present() {
        var cfg = new RequestIdFilterConfig();
        WebFilter f = cfg.requestIdFilter();

        var req = MockServerHttpRequest.get(PATH_P)
            .header(RequestIdFilterConfig.HEADER, GIVEN_1).build();
        var exchange = MockServerWebExchange.from(req);

        StepVerifier.create(f.filter(exchange, ex -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getHeaders().getFirst(RequestIdFilterConfig.HEADER))
            .isEqualTo(GIVEN_1);
        assertThat(exchange.getAttributes())
            .containsEntry(RequestIdFilterConfig.CONTEXT_KEY, GIVEN_1);

    }
}