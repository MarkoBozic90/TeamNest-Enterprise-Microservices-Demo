package com.teamnest.gateway.config;

import com.teamnest.shared.problem.ErrorCode;
import com.teamnest.shared.problem.ServiceException;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
public class TestControllers {

    @GetMapping("/__test/rls")
    public String rateLimited() {
        throw ServiceException.builder()
            .code(ErrorCode.RATE_LIMIT_EXCEEDED)
            .message("error.rateLimit")
            .build();
    }

    @GetMapping("/__stub/always-500")
    public Mono<String> always500() {
        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "boom"));
    }

    @GetMapping("/__fallback/users")
    public Mono<String> usersFallback() {
        return Mono.just("users-fallback");
    }

    @GetMapping("/__test/internal")
    public String internalBoom() {
        throw new RuntimeException("boom");
    }

    @GetMapping("/__fallback/trainings")
    public String trainingsFallback() {
        return "trainings-fallback";
    }

    private final AtomicInteger flakyCounter = new AtomicInteger(0);

    @GetMapping("/__stub/flaky-twice-then-ok")
    public String flakyTwiceThenOk() {
        int n = flakyCounter.incrementAndGet();
        if (n <= 2) {
            throw new RuntimeException("transient-500");
        }
        return "ok-after-retries";
    }


}
