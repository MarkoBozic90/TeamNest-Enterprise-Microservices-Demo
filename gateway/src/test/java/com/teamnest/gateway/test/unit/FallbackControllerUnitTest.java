package com.teamnest.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.teamnest.gateway.fallback.FallbackController;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class FallbackControllerUnitTest {

    FallbackController ctrl;

    @BeforeEach
    void setup() {
        var sms = new StaticMessageSource();
        sms.setUseCodeAsDefaultMessage(true);
        sms.addMessage("fallback.users.title",    Locale.ENGLISH, "Users down");
        sms.addMessage("fallback.users.detail",   Locale.ENGLISH, "Please retry later");
        sms.addMessage("fallback.trainings.title",Locale.forLanguageTag("sr"), "Treninzi nedostupni");
        sms.addMessage("fallback.trainings.detail",Locale.forLanguageTag("sr"), "Pokušajte kasnije");
        ctrl = new FallbackController(sms);
    }

    @Test
    void usersFallback_en_problem503_nostore_and_props() {
        var ex = MockServerWebExchange.from(MockServerHttpRequest.get("/__fallback/users").build());
        var resp = ctrl.usersFallback(Locale.ENGLISH, ex).block();

        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode().value()).isEqualTo(503);
        assertThat(resp.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(resp.getHeaders().getCacheControl()).isEqualTo("no-store");

        var pd = resp.getBody();
        assertThat(pd).isNotNull();
        assertThat(pd.getTitle()).isEqualTo("Users down");
        assertThat(pd.getDetail()).isEqualTo("Please retry later");
        Assertions.assertNotNull(pd.getInstance());
        assertThat(pd.getInstance().getPath()).isEqualTo("/__fallback/users");
        assertThat(pd.getProperties()).containsEntry("service", "user-service");
        assertThat(pd.getProperties()).containsEntry("app", "gateway");
    }

    @Test
    void trainingsFallback_sr_localized() {
        var ex = MockServerWebExchange.from(MockServerHttpRequest.get("/__fallback/trainings").build());
        var resp = ctrl.trainingsFallback(Locale.forLanguageTag("sr"), ex).block();

        assertThat(resp).isNotNull();
        assertThat(resp.getStatusCode().value()).isEqualTo(503);
        var pd = resp.getBody();
        assertThat(pd).isNotNull();
        assertThat(pd.getTitle()).isEqualTo("Treninzi nedostupni");
        assertThat(pd.getDetail()).isEqualTo("Pokušajte kasnije");
        assertThat(pd.getProperties()).containsEntry("service", "training-service");
    }
}