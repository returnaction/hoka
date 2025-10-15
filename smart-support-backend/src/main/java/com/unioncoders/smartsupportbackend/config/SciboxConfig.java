package com.unioncoders.smartsupportbackend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(SciboxProperties.class)
public class SciboxConfig {

    @Bean
    public RestClient sciboxRestClient(SciboxProperties props) {
        var rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout((int) Duration.ofSeconds(8).toMillis());
        rf.setReadTimeout((int) Duration.ofSeconds(20).toMillis());

        return RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(rf)
                .build();
    }
}