package com.example.product.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "product.service")
public class ProductServiceProperties {
    private int pageSize = 10;
    private boolean asyncEnabled = true;
    private boolean cacheEnabled = true;
    private long schedulingRateMs = 60000;
}
