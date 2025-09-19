package com.teamnest.gateway.test.it;

import static com.teamnest.gateway.constant.StringConstant.R1_TEST_ONE;
import static com.teamnest.gateway.constant.StringConstant.R1_TEST_TWO;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import({
    com.teamnest.gateway.config.TestControllers.class,
    com.teamnest.gateway.config.TestRoutesRateLimit.class,
    com.teamnest.gateway.trace.RequestIdFilterConfig.class,
    com.teamnest.gateway.config.KeyResolverConfig.class,
    com.teamnest.gateway.config.TestSecurityConfig.class,
    com.teamnest.gateway.config.TestRedisConfig.class
})
@Testcontainers(disabledWithoutDocker = true)
class RateLimiterGatewayItTest {

    @Autowired WebTestClient web;

    @Autowired ReactiveRedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void flushAll() {
        new ReactiveStringRedisTemplate(redisConnectionFactory)
            .execute(c -> c.serverCommands().flushAll())
            .then()
            .block();
    }
    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(
        DockerImageName.parse("redis:7.2-alpine"))
        .withExposedPorts(6379)
        .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)))
        .withStartupAttempts(3);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redisContainer.getHost());
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        ReactiveStringRedisTemplate redis = new ReactiveStringRedisTemplate(redisConnectionFactory);
        redis.execute(c -> c.serverCommands().flushAll()).then().block();
    }

    @Test
    void rateLimiter_blocks_when_exceeding_quota() {

        web.get().uri(R1_TEST_ONE)
            .exchange()
            .expectStatus().is5xxServerError();

        web.get().uri(R1_TEST_TWO)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
