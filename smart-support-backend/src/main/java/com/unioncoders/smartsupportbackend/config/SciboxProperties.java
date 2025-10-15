package com.unioncoders.smartsupportbackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scibox")
@Getter
@Setter
public class SciboxProperties {

    private String baseUrl;
    private String apiKey;

    private String embedModel = "bge-m3";
    private String chatModel  = "Qwen2.5-72B-Instruct-AWQ";
}
