package com.teamnest.gateway.test.it;

import com.teamnest.gateway.config.TestControllers;
import com.teamnest.gateway.config.TestMessageSourceConfig;
import com.teamnest.gateway.config.TestSecurityConfig;
import com.teamnest.gateway.error.GlobalExceptionHandler;
import com.teamnest.gateway.trace.RequestIdFilterConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.DisplayName;

import org.springframework.test.context.ActiveProfiles;


import static com.teamnest.gateway.constant.StringConstant.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@org.springframework.context.annotation.Import({
    GlobalExceptionHandler.class,
    TestControllers.class,
    TestMessageSourceConfig.class,
    TestSecurityConfig.class,
    RequestIdFilterConfig.class
})
class GlobalExceptionHandlerItTest {

    @Autowired WebTestClient web;

    @Test
    @DisplayName("ServiceException → HTTP_STATUS_429 EN with X-Request-Id and RFC7807 body")
    void serviceException_en() {
        var res = web.get().uri(TEST_RLS_URL)
            .header(HttpHeaders.ACCEPT_LANGUAGE, EN_LANGUAGE)
            .exchange()
            .expectStatus().isEqualTo(HTTP_STATUS_429)
            .expectHeader().exists(X_REQUEST_ID)
            .expectBody()
            .jsonPath(STATUS).isEqualTo(HTTP_STATUS_429)
            .jsonPath(TITLE).isEqualTo(TO_MANY_REQUESTS)
            .returnResult();
//[X-Request-Id:"03f83157-5ab6-4359-88e1-3b6240a86f0f", Content-Type:"application/problem+json", Retry-After:"5", Content-Length:"299", Cache-Control:"no-cache, no-store, max-age=0, must-revalidate", Pragma:"no-cache", Expires:"0", X-Content-Type-Options:"nosniff", X-Frame-Options:"DENY", X-XSS-Protection:"0", Referrer-Policy:"no-referrer"]
        assertThat(res.getResponseHeaders().getFirst(X_REQUEST_ID)).isNotBlank();
    }

    @Test
    @DisplayName("Throwable → 5xx SR with X-Request-Id and localized title")
    void throwable_sr() {
        var res = web.get().uri(TEST_INTERNAL_URL)
            .header(HttpHeaders.ACCEPT_LANGUAGE, SR_LANGUAGE)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectHeader().exists(X_REQUEST_ID)
            .expectBody()
            .jsonPath(TITLE).isEqualTo(UNEXPECTED_ERROR_SR)
            .returnResult();

        assertThat(res.getResponseHeaders().getFirst(X_REQUEST_ID)).isNotBlank();
    }

    @Test
    @DisplayName("Missing Accept-Language → default locale EN")
    void fallback_locale() {
        web.get().uri(TEST_RLS_URL)
            .exchange()
            .expectStatus().isEqualTo(HTTP_STATUS_429)
            .expectBody()
            .jsonPath(TITLE).value(v ->
                assertThat(v).asString().isIn(TO_MANY_REQUESTS, RATE_LIMIT_EXCEEDED)
            );
    }

}
