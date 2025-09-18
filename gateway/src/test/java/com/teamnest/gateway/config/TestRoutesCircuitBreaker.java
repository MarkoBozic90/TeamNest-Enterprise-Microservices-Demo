package com.teamnest.gateway.config;

import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRoutesCircuitBreaker {

    @Bean(destroyMethod = "shutdown")
    MockWebServer mockWebServer() throws IOException {
        MockWebServer mws = new MockWebServer();
        mws.start(); // ephemeral port
        return mws;
    }

    @Bean
    RouteLocator testRoutes(RouteLocatorBuilder builder, MockWebServer mws) {
        String base = mws.url("/").toString(); // e.g. http://127.0.0.1:54321/
        return builder.routes()
            .route("cb_always500", r -> r.path("/cb/always500/**")
                .filters(f -> f
                    .rewritePath("/cb/always500/(?<segment>.*)", "/users")
                    .circuitBreaker(c -> c
                        .setName("usersCB")
                        .setFallbackUri("forward:/__cb-fallback__")
                        .addStatusCode("500") // tretiraj 5xx kao failure
                    ))
                .uri(base))
            .build();
    }
}