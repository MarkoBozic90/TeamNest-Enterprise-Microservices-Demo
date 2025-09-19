package com.teamnest.gateway.test.it;

import static com.teamnest.gateway.constant.StringConstant.BOOM;
import static com.teamnest.gateway.constant.StringConstant.CB_ALWAYS_500_URL;
import static com.teamnest.gateway.constant.StringConstant.CIRCUIT_BREAKER;
import static com.teamnest.gateway.constant.StringConstant.HTTP_STATUS_500;
import static com.teamnest.gateway.constant.StringConstant.HTTP_STATUS_503;
import static com.teamnest.gateway.constant.StringConstant.SERVICE_UNAVAILABLE;
import static com.teamnest.gateway.constant.StringConstant.STATUS;
import static com.teamnest.gateway.constant.StringConstant.TITLE;
import static com.teamnest.gateway.constant.StringConstant.X_REQUEST_ID;

import com.teamnest.gateway.GatewayApplication;
import com.teamnest.gateway.config.TestRoutesCircuitBreaker;
import com.teamnest.gateway.config.TestSecurityConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GatewayApplication.class
)
@Import({
    TestSecurityConfig.class,
    TestRoutesCircuitBreaker.class,
})
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CircuitBreakerGatewayItTest {

    @Autowired
    WebTestClient client;

    @Autowired
    MockWebServer mock;

    @Test
    @DisplayName(CIRCUIT_BREAKER)
    void circuitBreaker_fallsBack_toUsersFallback() {
        // downstream 500
        mock.enqueue(new MockResponse().setResponseCode(HTTP_STATUS_500).setBody(BOOM));

        client.get().uri(CB_ALWAYS_500_URL)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .expectHeader().exists(X_REQUEST_ID)
            .expectBody()
            .jsonPath(TITLE).isEqualTo(SERVICE_UNAVAILABLE)
            .jsonPath(STATUS).isEqualTo(HTTP_STATUS_503);
    }
}