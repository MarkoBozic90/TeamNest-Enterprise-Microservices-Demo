package com.teamnest.userservice.infra;

import com.teamnest.userservice.port.TimeProvider;
import com.teamnest.userservice.port.TokenGenerator;
import com.teamnest.userservice.port.TokenHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityAdaptersConfig {

    @Bean
    public TokenGenerator tokenGenerator() {
        return new SecureRandomTokenGenerator();
    }

    @Bean
    public TokenHasher tokenHasher() {
        return new Sha256TokenHasher();
    }

    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }
}