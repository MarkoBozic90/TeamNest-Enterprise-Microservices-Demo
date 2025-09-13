package com.teamnest.gateway.error;


import com.teamnest.shared.problem.ErrorCode;
import com.teamnest.shared.problem.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = { GlobalExceptionHandler.class, GlobalExceptionHandlerTest.DemoCtrl.class })
@AutoConfigureWebTestClient
class GlobalExceptionHandlerTest {

    @Autowired WebTestClient web;


    @RestController
    static class DemoCtrl {
        @GetMapping("/__test/rls")
        public String rls() { throw ServiceException.builder()
            .code(ErrorCode.RATE_LIMIT_EXCEEDED)
            .message("error.rateLimit")
            .build();
        }
        @GetMapping("/__test/internal")
        public String internal() { throw new RuntimeException("boom"); }
    }

    @Test
    void serviceException_isProblemDetail_en() {

        var body = web.get().uri("/__test/rls")
            .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
            .exchange()
            .expectStatus().isEqualTo(429)
            .expectBody(ProblemDetail.class).returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(429);
        assertThat(body.getTitle()).isIn("Too many requests", "Rate limit exceeded"); // i18n title
    }

    @Test
    void throwable_isProblemDetail_sr() {
        var body = web.get().uri("/__test/internal")
            .header(HttpHeaders.ACCEPT_LANGUAGE, "sr")
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody(ProblemDetail.class).returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isIn("Neočekivana greška", "Unexpected error");
    }
}
