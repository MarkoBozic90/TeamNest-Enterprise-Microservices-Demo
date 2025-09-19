package com.teamnest.gateway.test.it;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import com.teamnest.gateway.error.GlobalExceptionHandler;
import com.teamnest.gateway.error.ProblemFactory;
import com.teamnest.gateway.fallback.FallbackController;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(
    controllers = FallbackController.class,
    excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        ReactiveOAuth2ClientAutoConfiguration.class,
        ReactiveOAuth2ResourceServerAutoConfiguration.class
    }
)
@ActiveProfiles("dev")
@Import({ FallbackControllerWebFluxItTest.MsgCfg.class, ProblemFactory.class, GlobalExceptionHandler.class })
class FallbackControllerWebFluxItTest {

    @Autowired WebTestClient web;

    @TestConfiguration
    static class MsgCfg {
        @Bean
        StaticMessageSource messageSource() {
            var sms = new StaticMessageSource();
            sms.setUseCodeAsDefaultMessage(true);
            sms.addMessage("fallback.users.title",      Locale.ENGLISH, "Users down");
            sms.addMessage("fallback.users.detail",     Locale.ENGLISH, "Please retry later");
            sms.addMessage("fallback.trainings.title",  Locale.ENGLISH, "Trainings down");
            sms.addMessage("fallback.trainings.detail", Locale.ENGLISH, "Retry later");
            return sms;
        }
    }

    @Test
    void usersFallback_returns_503_problem_noStore() {
        web.get().uri("/__fallback/users")
            .exchange()
            .expectStatus().isEqualTo(503)
            .expectHeader().contentType(APPLICATION_PROBLEM_JSON)
            .expectHeader().valueEquals("Cache-Control", "no-store")
            .expectBody()
            .jsonPath("$.title").isEqualTo("Service unavailable")
            .jsonPath("$.detail").isEqualTo("User service temporarily unavailable, please retry")
            .jsonPath("$.service").isEqualTo("user-service");
    }

    @Test
    void trainingsFallback_returns_503_problem() {
        web.get().uri("/__fallback/trainings")
            .exchange()
            .expectStatus().isEqualTo(503)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Service unavailable")
            .jsonPath("$.detail").isEqualTo("Training service temporarily unavailable, please retry")
            .jsonPath("$.service").isEqualTo("training-service");
    }
}
