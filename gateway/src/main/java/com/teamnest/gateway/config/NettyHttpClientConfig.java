package com.teamnest.gateway.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;

@Configuration
public class NettyHttpClientConfig {

    // Ako koristiš i embedded Netty server tuning (opciono)
    @Bean
    ReactiveWebServerFactory reactiveWebServerFactory() {
        return new NettyReactiveWebServerFactory();
    }

    // Gateway HttpClient tuning – zamena za deprecated properties
    @Bean
    HttpClientCustomizer gatewayHttpClientTimeouts() {
        return (HttpClient http) -> http
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofSeconds(5));
    }
}
