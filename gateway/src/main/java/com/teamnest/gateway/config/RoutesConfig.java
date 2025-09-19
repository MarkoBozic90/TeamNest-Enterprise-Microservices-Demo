package com.teamnest.gateway.config;

import java.time.Duration;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.DedupeResponseHeaderGatewayFilterFactory.Strategy;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;


/** Java DSL routes: lb:// via Eureka, Resilience4j CB, Retry, Redis RL, security headers. */
@Profile("prod")
@Configuration
public class RoutesConfig {

    private static final  String COOKIE_NAME = "cookie";
    private static final  String SWAGGER_PATH = "/swagger-ui/index.html";

    // RL beans per route group
    @Bean
    public RedisRateLimiter rlUsers() {
        return new RedisRateLimiter(10, 20);
    }

    @Bean
    public RedisRateLimiter rlTraining() {
        return new RedisRateLimiter(20, 40);
    }

    @Bean
    public RedisRateLimiter rlBff()      {
        return new RedisRateLimiter(50, 100);
    }

    // Global security headers (what you had as default-filters)
    @Bean
    public GlobalFilter securityHeaders() {
        return (exchange, chain) -> {
            var h = exchange.getResponse().getHeaders();
            h.set("X-Content-Type-Options", "nosniff");
            h.set("X-Frame-Options", "DENY");
            h.set("Referrer-Policy", "no-referrer");
            h.set("Content-Security-Policy", "default-src 'self'");
            return chain.filter(exchange);
        };
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder rlb,
                               KeyResolverConfig resolvers,
                               RedisRateLimiter rlUsers,
                               RedisRateLimiter rlTraining,
                               RedisRateLimiter rlBff) {
        return rlb.routes()

            // ---------- User Service ----------
            .route("user-service", r -> r
                .path("/api/v1/users/**", "/api/v1/registrations/**")
                .filters(f -> f
                    .dedupeResponseHeader("Access-Control-Allow-Credentials", Strategy.RETAIN_FIRST.name())
                    .dedupeResponseHeader("Access-Control-Allow-Origin",      Strategy.RETAIN_FIRST.name())
                    .dedupeResponseHeader("Access-Control-Expose-Headers",    Strategy.RETAIN_FIRST.name())
                    .requestRateLimiter(c -> {
                        c.setKeyResolver(resolvers.principalOrIpKeyResolver());
                        c.setRateLimiter(rlUsers);
                    })
                    .circuitBreaker(c -> c.setName("userServiceCB")
                        .setFallbackUri("forward:/__fallback/users"))
                    .removeRequestHeader(COOKIE_NAME)
                    .preserveHostHeader()
                    .retry(c -> {
                        c.setRetries(3);
                        c.setMethods(HttpMethod.GET);
                        c.setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
                        c.setBackoff(Duration.ofMillis(50), Duration.ofMillis(50), 1, false); // "linear"
                    })
                    .dedupeResponseHeader(
                        "Access-Control-Allow-Credentials "
                            + "Access-Control-Allow-Origin Access-Control-Expose-Headers", Strategy.RETAIN_FIRST.name())
                )
                .uri("lb://user-service"))

            // ---------- Training Service ----------
            .route("training-service", r -> r
                .path("/api/v1/trainings/**", "/api/v1/rsvp/**")
                .filters(f -> f
                    .requestRateLimiter(c -> {
                        c.setKeyResolver(resolvers.principalOrIpKeyResolver());
                        c.setRateLimiter(rlTraining);
                    })
                    .circuitBreaker(c -> c.setName("trainingServiceCB")
                        .setFallbackUri("forward:/__fallback/trainings"))
                    .removeRequestHeader(COOKIE_NAME)
                    .preserveHostHeader()
                    .retry(c -> {
                        c.setRetries(2);
                        c.setMethods(HttpMethod.GET);
                        c.setSeries(HttpStatus.Series.SERVER_ERROR);
                        c.setBackoff(Duration.ofMillis(100), Duration.ofSeconds(1), 1, true); // exponential
                    })
                )
                .uri("lb://training-service"))

            // ---------- BFF (directory) ----------
            .route("directory-bff", r -> r
                .path("/api/v1/directory/**")
                .filters(f -> f
                    .requestRateLimiter(c -> {
                        c.setKeyResolver(resolvers.principalOrIpKeyResolver());
                        c.setRateLimiter(rlBff);
                    })
                    .removeRequestHeader(COOKIE_NAME)
                    .preserveHostHeader()
                )
                .uri("lb://directory-bff"))

            // ---------- Swagger helpers ----------
            .route("user-swagger", r -> r
                .path("/docs/user/**", "/docs/user")
                .filters(f -> f.setPath(SWAGGER_PATH))
                .uri("lb://user-service"))
            .route("training-swagger", r -> r
                .path("/docs/training/**", "/docs/training")
                .filters(f -> f.setPath(SWAGGER_PATH))
                .uri("lb://training-service"))
            .route("bff-swagger", r -> r
                .path("/docs/bff/**", "/docs/bff")
                .filters(f -> f.setPath(SWAGGER_PATH))
                .uri("lb://directory-bff"))

            .build();
    }
}
