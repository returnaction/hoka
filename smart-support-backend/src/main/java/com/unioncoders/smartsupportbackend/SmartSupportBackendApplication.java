package com.unioncoders.smartsupportbackend;

import com.unioncoders.smartsupportbackend.config.SciboxProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SciboxProperties.class)
public class SmartSupportBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSupportBackendApplication.class, args);
    }
}

