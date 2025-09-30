package com.teamnest.gateway.fallback;

import com.teamnest.gateway.FallbackProblemParams;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Profile({"dev", "prod"})
@RestController
@RequiredArgsConstructor
public class FallbackController {

    private final MessageSource messages;

    @GetMapping(value = "/__fallback/users", produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    public Mono<ResponseEntity<ProblemDetail>> usersFallback(final Locale locale, final ServerWebExchange ex) {
        return Mono.just(buildFallback(new FallbackProblemParams(
            locale, ex,
            "user-service",
            "fallback.users.title",   "Users down",
            "fallback.users.detail",  "User service temporarily unavailable, please retry",
            URI.create("about:blank#user-service-unavailable"))
        ));
    }

    @GetMapping(value = "/__fallback/trainings", produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    public Mono<ResponseEntity<ProblemDetail>> trainingsFallback(final Locale locale, final ServerWebExchange ex) {
        return Mono.just(buildFallback(new FallbackProblemParams(
            locale, ex,
            "training-service",
            "fallback.trainings.title", "Trainings down",
            "fallback.trainings.detail", "Training service temporarily unavailable, please retry",
            URI.create("about:blank#training-service-unavailable")
        )));
    }

    private ResponseEntity<ProblemDetail> buildFallback(
        final FallbackProblemParams fallbackProblemParams) {
        var pd = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        pd.setType(fallbackProblemParams.type());
        pd.setTitle(messages.getMessage(fallbackProblemParams.titleKey(),
            null, fallbackProblemParams.titleDefault(), fallbackProblemParams.locale()));
        pd.setDetail(messages.getMessage(fallbackProblemParams.detailKey(),
            null, fallbackProblemParams.detailDefault(), fallbackProblemParams.locale()));
        pd.setInstance(URI.create(fallbackProblemParams.exchange().getRequest().getPath().value()));
        pd.setProperty("service", fallbackProblemParams.serviceName());
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        pd.setProperty("app", "gateway");

        return ResponseEntity
            .status(pd.getStatus())
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .cacheControl(CacheControl.noStore())
            .body(pd);
    }
}
