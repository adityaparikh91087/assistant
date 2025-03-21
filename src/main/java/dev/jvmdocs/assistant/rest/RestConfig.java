package dev.jvmdocs.assistant.rest;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder, ClientLoggerRequestInterceptor requestInterceptor) {
        return builder
                .requestInterceptor(requestInterceptor)
                .build();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setReadTimeout(Duration.ofSeconds(30));
            factory.setConnectTimeout(Duration.ofSeconds(30));
            restClientBuilder.requestFactory(factory);
        };
    }
}
