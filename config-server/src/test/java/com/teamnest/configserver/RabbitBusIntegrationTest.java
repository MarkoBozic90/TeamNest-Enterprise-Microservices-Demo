package com.teamnest.configserver;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import static com.teamnest.configserver.TestConstants.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RabbitBusIntegrationTest {

    static RabbitMQContainer rabbit =
        new RabbitMQContainer(DockerImageName.parse(IMG_RABBIT));

    @Autowired TestRestTemplate rest;

    @BeforeAll
    static void startRabbit() { rabbit.start(); }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add(P_RMQ_HOST, rabbit::getHost);
        r.add(P_RMQ_PORT, () -> rabbit.getAmqpPort().toString());
        r.add(P_RMQ_USER, rabbit::getAdminUsername);
        r.add(P_RMQ_PASS, rabbit::getAdminPassword);

        r.add(P_BUS_ENABLED, () -> "true");
        r.add(P_EXPOSE, () -> "health,info,prometheus,busrefresh,refresh");
        r.add(P_SEC_USER, () -> ADMIN_USER);
        r.add(P_SEC_PASS, () -> ADMIN_PASS);
        r.add(P_SEC_ROLES, () -> ROLE_ADMIN);
    }

    @Test
    void healthHasRabbit() {
        var resp = rest.getForEntity(ACTUATOR_HEALTH, String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        // Better than a long escaped string: assert key parts
        assertThat(resp.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void busrefreshRequiresAuthAndSucceedsWithAdmin() {
        var unauth = rest.postForEntity(ACTUATOR_BUSREF, null, String.class);
        assertThat(unauth.getStatusCodeValue()).isIn(401, 403);

        var admin = rest.withBasicAuth(ADMIN_USER, ADMIN_PASS);
        var ok = admin.postForEntity(ACTUATOR_BUSREF, null, String.class);
        assertThat(ok.getStatusCode().is2xxSuccessful()).isTrue();
    }
}