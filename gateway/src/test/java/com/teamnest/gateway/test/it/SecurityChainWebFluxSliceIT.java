package com.teamnest.gateway.test.it;

import static com.teamnest.gateway.constant.StringConstant.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = SecurityChainWebFluxSliceIT.TestEndpoints.class)
@Import({ SecurityChainWebFluxSliceIT.TestSecurity.class , SecurityChainWebFluxSliceIT.TestEndpoints.class })
@ImportAutoConfiguration(WebFluxAutoConfiguration.class)
@AutoConfigureWebTestClient
class SecurityChainWebFluxSliceIT {

    @Autowired WebTestClient web;

    @Test void docs_open()     { web.get().uri(PATH_DOCS_PING).exchange().expectStatus().isOk(); }
    @Test void fallback_open() { web.get().uri(PATH_FALLBACK_PING).exchange().expectStatus().isOk(); }
    @Test void options_open()  { web.options().uri(PATH_PROTECTED).exchange().expectStatus().isOk(); }

    @Test
    void protected_requires_jwt() {
        web.get().uri(PATH_PROTECTED).exchange().expectStatus().isUnauthorized();
    }

    @Test
    void protected_with_jwt_ok() {
        web.mutateWith(mockJwt().jwt(j -> j.claim(CLAIM_ROLES, List.of(ROLE_USER))))
            .get().uri(PATH_PROTECTED)
            .exchange().expectStatus().isOk();
    }

    @Configuration
    static class TestSecurity {
        @Bean
        SecurityWebFilterChain chain(ServerHttpSecurity http) {
            return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .anonymous(ServerHttpSecurity.AnonymousSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(reg -> reg
                    .pathMatchers("/actuator/**","/__fallback/**","/docs/**").permitAll()
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyExchange().authenticated()
                )
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .build();
        }
    }

    @RestController
    static class TestEndpoints {
        @GetMapping(PATH_DOCS_PING)     Mono<String> docs() { return Mono.just(BODY_OK); }
        @GetMapping(PATH_FALLBACK_PING) Mono<String> fb()   { return Mono.just(BODY_OK); }
        @GetMapping(PATH_PROTECTED)     Mono<String> prot() { return Mono.just(BODY_OK); }
        @RequestMapping(value = PATH_PROTECTED, method = RequestMethod.OPTIONS)
        ResponseEntity<Void> options() { return ResponseEntity.ok().build(); }
    }
}
