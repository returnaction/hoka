package com.unioncoders.smartsupportbackend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(CorsProperties props) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                var reg = registry.addMapping("/api/**")
                        .allowedMethods(props.getAllowedMethods().toArray(String[]::new))
                        .allowedHeaders(props.getAllowedHeaders().toArray(String[]::new))
                        .allowCredentials(props.isAllowCredentials())
                        .maxAge(3600);

                if (!props.getAllowedOrigins().isEmpty()) {
                    reg.allowedOrigins(props.getAllowedOrigins().toArray(String[]::new));
                }
            }
        };
    }
}
