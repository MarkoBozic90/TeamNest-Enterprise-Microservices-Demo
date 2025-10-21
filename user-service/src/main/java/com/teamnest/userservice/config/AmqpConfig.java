package com.teamnest.userservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {
    @Bean
    public TopicExchange inviteEventsExchange(@Value("${app.messaging.exchanges.invite}") String name) {
        return new TopicExchange(name, true, false);
    }
}