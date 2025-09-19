package com.teamnest.gateway.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.oauth2.server.resource.web.access.server.BearerTokenServerAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .anonymous(ServerHttpSecurity.AnonymousSpec::disable)

            .authorizeExchange(reg -> reg
                .pathMatchers("/actuator/**", "/__fallback/**", "/docs/**").permitAll()
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyExchange().authenticated()
            )
            .headers(h -> h
                .contentTypeOptions(c -> {

                }) // X-Content-Type-Options: nosniff
                .frameOptions(fo -> fo.mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY)) // X-Frame-Options: DENY
                .referrerPolicy(rp -> rp.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.NO_REFERRER))
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                // opcionalno:
                .permissionsPolicy(pp -> pp.policy("geolocation=(), microphone=()"))
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new BearerTokenServerAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenServerAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(reactiveJwtAuthConverter()))
            );
        return http.build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter reactiveJwtAuthConverter() {
        var delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(this::mapRolesToAuthorities);
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(final Jwt jwt) {
        var roles = extractRoles(jwt);
        return toAuthorities(roles);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(final Jwt jwt) {
        // 1) "roles" (custom claim)
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            return roles;
        }

        // 2) Keycloak: realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) {
            return List.of();
        }

        Object raw = realmAccess.get("roles");
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>(list.size());
            for (Object o : list) {
                if (o != null) {
                    out.add(o.toString());
                }
            }
            return out; // << bugfix: vrati posle petlje, ne unutra
        }
        return List.of();
    }

    private Collection<GrantedAuthority> toAuthorities(final List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        List<GrantedAuthority> out = new ArrayList<>(roles.size());
        for (String r : roles) {
            String name = (r != null && r.startsWith("ROLE_")) ? r : "ROLE_" + r;
            out.add(new SimpleGrantedAuthority(name));
        }
        return out;
    }
}