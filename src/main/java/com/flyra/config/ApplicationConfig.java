package com.flyra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class ApplicationConfig {
    private String role = "coordinator"; // coordinator or application
    private String podId;
    private String coordinatorUrl = "http://localhost:8080";
    private int applicationPort = 8081;
}

