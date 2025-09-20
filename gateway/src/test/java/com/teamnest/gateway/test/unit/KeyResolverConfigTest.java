package com.teamnest.gateway.test.unit;

import static com.teamnest.gateway.constant.StringConstant.ALICE;
import static com.teamnest.gateway.constant.StringConstant.ANONYMOUS;
import static com.teamnest.gateway.constant.StringConstant.PATH_IO;
import static com.teamnest.gateway.constant.StringConstant.PATH_LOCAL_IP;
import static com.teamnest.gateway.constant.StringConstant.X_FORWARDED_FOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.teamnest.gateway.config.KeyResolverConfig;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class KeyResolverConfigTest {

    private final KeyResolverConfig cfg = new KeyResolverConfig();
    private final KeyResolver resolver = cfg.principalOrIpKeyResolver();

    @Test
    void resolves_by_principal_name_when_present() {
        ServerWebExchange ex = mock(ServerWebExchange.class);
        when(ex.getPrincipal()).thenReturn(Mono.just((Principal) () -> ALICE));
        // request potreban, ali bez spec. headera
        when(ex.getRequest()).thenReturn(MockServerHttpRequest.get("/").build());

        String key = resolver.resolve(ex).block();
        assertThat(key).isEqualTo(ALICE);
    }

    @Test
    void resolves_by_x_forwarded_for_when_no_principal() {
        var req = MockServerHttpRequest.get("/")
            .header(X_FORWARDED_FOR, PATH_IO).build();
        var ex = MockServerWebExchange.from(req);

        String key = resolver.resolve(ex).block();
        assertThat(key).isEqualTo(PATH_IO);
    }

    @Test
    void resolves_by_remote_address_when_no_principal_nor_xff() throws Exception {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(
            new InetSocketAddress(InetAddress.getByName(PATH_LOCAL_IP), 0)
        );

        ServerWebExchange ex = mock(ServerWebExchange.class);
        when(ex.getPrincipal()).thenReturn(Mono.empty());
        when(ex.getRequest()).thenReturn(req);

        String key = resolver.resolve(ex).block();
        assertThat(key).isEqualTo(PATH_LOCAL_IP);
    }

    @Test
    void falls_back_to_anonymous_when_all_missing() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        when(req.getHeaders()).thenReturn(new HttpHeaders());
        when(req.getRemoteAddress()).thenReturn(null);

        ServerWebExchange ex = mock(ServerWebExchange.class);
        when(ex.getPrincipal()).thenReturn(Mono.empty());
        when(ex.getRequest()).thenReturn(req);

        String key = resolver.resolve(ex).block();
        assertThat(key).isEqualTo(ANONYMOUS);
    }
}