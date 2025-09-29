package com.teamnest.configserver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestConstants {

    // Security / users
    public static final String ADMIN_USER = "config";
    public static final String ADMIN_PASS = "changeme";
    public static final String ROLE_ADMIN  = "ADMIN";

    // Actuator (you can keep inline, but constants help reuse in multiple tests)
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ACTUATOR_PROM   = "/actuator/prometheus";
    public static final String ACTUATOR_BUSREF = "/actuator/busrefresh";
    public static final String ACTUATOR_REFRESH= "/actuator/refresh";

    // Testcontainers images
    public static final String IMG_RABBIT = "rabbitmq:3.13-management";
    public static final String IMG_VAULT  = "hashicorp/vault:1.16";

    // Vault
    public static final String VAULT_TOKEN = "root";
    public static final int    VAULT_PORT  = 8200;

    // Common property keys you set via @DynamicPropertySource
    public static final String P_RMQ_HOST = "spring.rabbitmq.host";
    public static final String P_RMQ_PORT = "spring.rabbitmq.port";
    public static final String P_RMQ_USER = "spring.rabbitmq.username";
    public static final String P_RMQ_PASS = "spring.rabbitmq.password";
    public static final String P_BUS_ENABLED = "spring.cloud.bus.enabled";
    public static final String P_EXPOSE = "management.endpoints.web.exposure.include";
    public static final String P_SEC_USER = "spring.security.user.name";
    public static final String P_SEC_PASS = "spring.security.user.password";
    public static final String P_SEC_ROLES= "spring.security.user.roles";

    public static final String P_V_IMPORT = "spring.config.import";
    public static final String P_V_SCHEME = "spring.cloud.vault.scheme";
    public static final String P_V_HOST   = "spring.cloud.vault.host";
    public static final String P_V_PORT   = "spring.cloud.vault.port";
    public static final String P_V_TOKEN  = "spring.cloud.vault.token";
    public static final String P_V_BACKEND= "spring.cloud.vault.kv.backend";
    public static final String P_V_SEP    = "spring.cloud.vault.kv.profile-separator";
}