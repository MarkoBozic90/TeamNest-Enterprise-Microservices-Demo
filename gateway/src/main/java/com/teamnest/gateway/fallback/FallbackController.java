package com.teamnest.gateway.fallback;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
public class FallbackController {

    @GetMapping(value = "/__fallback/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> usersFallback() {
        return Mono.just(Map.of(
            "status", "degraded",
            "service", "user-service",
            "message", "Temporarily unavailable, please retry"
        ));
    }

    @GetMapping(value = "/__fallback/trainings", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> trainingsFallback() {
        return Mono.just(Map.of(
            "status", "degraded",
            "service", "training-service",
            "message", "Temporarily unavailable, please retry"
        ));
    }
}
