package com.teamnest.gateway.test.unit;

import  static com.teamnest.gateway.constant.StringConstant.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.teamnest.gateway.security.SecurityConfig;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
class SecurityConfigTest {

    @Test
    void maps_roles_claim_with_ROLE_prefix() {
        var adapter = new SecurityConfig().reactiveJwtAuthConverter();

        var jwt = Jwt.withTokenValue(JWT_TOKEN_VALUE)
            .header(JWT_HEADER_ALG, JWT_ALG_NONE)
            .claim(CLAIM_ROLES, List.of(ROLE_ADMIN, ROLE_MANAGER))
            .build();

        Authentication auth = adapter.convert(jwt).block();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities())
            .extracting("authority")
            .containsExactlyInAnyOrder(AUTH_ROLE_ADMIN, AUTH_ROLE_MANAGER);
    }

    @Test
    void maps_realm_access_roles() {
        var adapter = new SecurityConfig().reactiveJwtAuthConverter();

        var jwt = Jwt.withTokenValue(JWT_TOKEN_VALUE)
            .header(JWT_HEADER_ALG, JWT_ALG_NONE)
            .claim(CLAIM_REALM_ACCESS, Map.of(CLAIM_REALM_ACCESS_ROLES, List.of(ROLE_USER, ROLE_SUPER)))
            .build();

        var auth = adapter.convert(jwt).block();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities())
            .extracting("authority")
            .containsExactlyInAnyOrder(AUTH_ROLE_USER, AUTH_ROLE_SUPER);
    }

    @Test
    void no_roles_yields_empty_authorities() {
        var adapter = new SecurityConfig().reactiveJwtAuthConverter();

        var jwt = Jwt.withTokenValue(JWT_TOKEN_VALUE)
            .header(JWT_HEADER_ALG, JWT_ALG_NONE)
            .claim(CLAIM_OTHER_KEY, CLAIM_OTHER_VAL)
            .build();

        var auth = adapter.convert(jwt).block();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
        // (opciono) potvrdi da je claim ostao u JWT-u
        assertThat(jwt.getClaims()).containsEntry(CLAIM_OTHER_KEY, CLAIM_OTHER_VAL);
    }
}