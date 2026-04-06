package com.example.order.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AppConfig {

     @Value("${app.environment}")
    private String environment;

    @Value("${app.api.timeout:5000}")
    private long apiTimeout;

    @Value("${server.port:8080}")
    private Integer serverPort;


    // Getters
    public String getEnvironment() {
        return environment;
    }

    public long getApiTimeout() {
        return apiTimeout;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    /**
     * RestTemplate Bean for inter-service communication (Synchronous Call)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public String toString() {
        return "ApplicationProperties{" +
                "environment='" + environment + '\'' +
                ", apiTimeout=" + apiTimeout +
                ", serverPort=" + serverPort +
                '}';
    }
}
