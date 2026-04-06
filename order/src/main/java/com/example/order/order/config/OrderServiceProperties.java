package com.example.order.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "order.service")
public class OrderServiceProperties {
    private String productServiceUrl = "http://localhost:8080/v1/products/";
    private int pageSize = 10;
    private boolean asyncEnabled = true;
    private boolean cacheEnabled = true;
    private long schedulingRateMs = 60000;
}
