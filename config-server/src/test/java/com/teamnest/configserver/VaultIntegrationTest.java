package com.teamnest.configserver;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static com.teamnest.configserver.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VaultIntegrationTest {

    static GenericContainer<?> vault =
        new GenericContainer<>(DockerImageName.parse(IMG_VAULT))
            .withEnv("VAULT_DEV_ROOT_TOKEN_ID", VAULT_TOKEN)
            .withEnv("VAULT_DEV_LISTEN_ADDRESS", "0.0.0.0:" + VAULT_PORT)
            .withExposedPorts(VAULT_PORT);

    @Autowired TestRestTemplate rest;

    @BeforeAll
    static void startVaultAndSeed() {
        vault.start();

        String base = "http://" + vault.getHost() + ":" + vault.getMappedPort(VAULT_PORT);
        var rt = new RestTemplate();

        var h = new HttpHeaders();
        h.set("X-Vault-Token", VAULT_TOKEN);
        h.setContentType(MediaType.APPLICATION_JSON);
        String body = """
      {"data":{
        "CONFIG_GIT_URI":"file:///tmp/dummy",
        "CONFIG_GIT_BRANCH":"main",
        "CONFIG_GIT_USERNAME":"x",
        "CONFIG_GIT_PASSWORD":"y"
      }}
      """;
        rt.exchange(base + "/v1/secret/data/config-server", HttpMethod.POST, new HttpEntity<>(body, h), String.class);
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        String host = vault.getHost();
        Integer port = vault.getMappedPort(VAULT_PORT);
        r.add(P_V_IMPORT, () -> "vault://");
        r.add(P_V_SCHEME, () -> "http");
        r.add(P_V_HOST, () -> host);
        r.add(P_V_PORT, () -> port);
        r.add(P_V_TOKEN, () -> VAULT_TOKEN);
        r.add(P_V_BACKEND, () -> "secret");
        r.add(P_V_SEP, () -> "/");
    }

    @Test
    void contextLoadsWithVault() {
        var health = rest.getForEntity(ACTUATOR_HEALTH, String.class);
        assertThat(health.getStatusCode().is2xxSuccessful()).isTrue();
    }
}