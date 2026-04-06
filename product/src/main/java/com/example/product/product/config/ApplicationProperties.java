package com.example.product.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {

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

    @Override
    public String toString() {
        return "ApplicationProperties{" +
                "environment='" + environment + '\'' +
                ", apiTimeout=" + apiTimeout +
                ", serverPort=" + serverPort +
                '}';
    }
}
