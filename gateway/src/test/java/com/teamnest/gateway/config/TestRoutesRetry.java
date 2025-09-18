// src/test/java/com/teamnest/gateway/config/TestRoutesRetry.java
package com.teamnest.gateway.config;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

@TestConfiguration
public class TestRoutesRetry {

    @Bean
    RouteLocator testRoutes(RouteLocatorBuilder rlb, Environment env) {
        String target = env.getProperty("gateway.test.retryTarget");
        Assert.hasText(target, "Missing property gateway.test.retryTarget (WireMock baseUrl)");

        return rlb.routes()
            .route("retry-downstream", r -> r
                .path("/retry/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .retry(cfg -> {
                        cfg.setRetries(2);
                        cfg.setMethods(HttpMethod.GET);
                        cfg.setSeries(HttpStatus.Series.SERVER_ERROR);
                        cfg.setBackoff(Duration.ofMillis(20), Duration.ofMillis(100), 2, false);
                    })
                )
                .uri(target)
            )
            .build();
    }
}
