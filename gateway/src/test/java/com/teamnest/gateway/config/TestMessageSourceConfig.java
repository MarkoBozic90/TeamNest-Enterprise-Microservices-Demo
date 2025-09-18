package com.teamnest.gateway.config;

import static com.teamnest.gateway.constant.StringConstant.*;

import java.util.Locale;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.StaticMessageSource;

@TestConfiguration
public class TestMessageSourceConfig {

    private static final Locale LOCALE_EN = Locale.ENGLISH;
    private static final Locale LOCALE_SR = Locale.forLanguageTag(SR_LANGUAGE); // "sr", ili "sr-Latn"/"sr-Cyrl" po potrebi

    @Bean
    MessageSource messageSource() {
        var sms = new StaticMessageSource();
        sms.setUseCodeAsDefaultMessage(true);

        // 429
        sms.addMessage("error.rateLimit", LOCALE_EN, TO_MANY_REQUESTS);
        sms.addMessage("error.rateLimit", LOCALE_SR, TO_MANY_REQUESTS_SR);

        // 401
        sms.addMessage("error.auth.failed", LOCALE_EN, "Authentication failed");

        // 500
        sms.addMessage("error.internal", LOCALE_EN, UNEXPECTED_ERROR);
        sms.addMessage("error.internal", LOCALE_SR, UNEXPECTED_ERROR_SR);

        return sms;
    }
}