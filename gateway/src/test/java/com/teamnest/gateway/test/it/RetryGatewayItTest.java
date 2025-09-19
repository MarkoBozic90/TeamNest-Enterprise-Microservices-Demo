package com.teamnest.gateway.test.it;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static com.teamnest.gateway.constant.StringConstant.FLAKY;
import static com.teamnest.gateway.constant.StringConstant.FLAKY_TEST_URL;
import static com.teamnest.gateway.constant.StringConstant.HTTP_STATUS_200;
import static com.teamnest.gateway.constant.StringConstant.HTTP_STATUS_500;
import static com.teamnest.gateway.constant.StringConstant.OK;
import static com.teamnest.gateway.constant.StringConstant.OK_AFTER_RETRIES;
import static com.teamnest.gateway.constant.StringConstant.RETRY_FLAKY_TEST_URL;
import static com.teamnest.gateway.constant.StringConstant.STEP_2;
import static com.teamnest.gateway.constant.StringConstant.X_REQUEST_ID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import({
    com.teamnest.gateway.config.TestRoutesRetry.class,
    com.teamnest.gateway.config.TestSecurityConfig.class,
    com.teamnest.gateway.trace.RequestIdFilterConfig.class,
    com.teamnest.gateway.config.KeyResolverConfig.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RetryGatewayItTest {

    @Autowired WebTestClient web;

    private static final WireMockServer WM = new WireMockServer(wireMockConfig().dynamicPort());

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        if (!WM.isRunning()) WM.start();             
        r.add("gateway.test.retryTarget", WM::baseUrl); 
    }

    @AfterAll
    void stopWm() { if (WM.isRunning()) WM.stop(); }

    @Test
    void retry_eventually_succeeds_after_transient_errors() {
        WM.resetAll();
        // 500 -> 500 -> 200
        WM.stubFor(get(urlEqualTo(FLAKY_TEST_URL))
            .inScenario(FLAKY).whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(HTTP_STATUS_500))
            .willSetStateTo(STEP_2));
        WM.stubFor(get(urlEqualTo(FLAKY_TEST_URL))
            .inScenario(FLAKY).whenScenarioStateIs(STEP_2)
            .willReturn(aResponse().withStatus(HTTP_STATUS_500))
            .willSetStateTo(OK));
        WM.stubFor(get(urlEqualTo(FLAKY_TEST_URL))
            .inScenario(FLAKY).whenScenarioStateIs(OK)
            .willReturn(aResponse().withStatus(HTTP_STATUS_200).withBody(OK_AFTER_RETRIES)));

        web.get().uri(RETRY_FLAKY_TEST_URL)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists(X_REQUEST_ID)
            .expectBody(String.class).isEqualTo(OK_AFTER_RETRIES);

        WM.verify(3, getRequestedFor(urlEqualTo(FLAKY_TEST_URL)));

    }

   
}