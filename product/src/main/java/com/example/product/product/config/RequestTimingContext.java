package com.example.product.product.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RequestTimingContext {

    private static final Logger logger = LoggerFactory.getLogger(RequestTimingContext.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final String requestId = UUID.randomUUID().toString();
    private final LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime;

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void logStartTime() {
        logger.info("[REQUEST START] RequestID: {} | Time: {} | Timestamp: {} ms", 
            requestId, 
            startTime.format(formatter),
            System.currentTimeMillis());
    }

    public void logEndTime() {
        this.endTime = LocalDateTime.now();
        long duration = System.currentTimeMillis() - getStartTimeMillis();
        
        logger.info("[REQUEST END] RequestID: {} | Time: {} | Duration: {} ms | Timestamp: {} ms", 
            requestId,
            endTime.format(formatter),
            duration,
            System.currentTimeMillis());
    }

    private long getStartTimeMillis() {
        // Calculate start time in milliseconds from LocalDateTime
        return startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @Override
    public String toString() {
        return "RequestTimingContext{" +
                "requestId='" + requestId + '\'' +
                ", startTime=" + startTime.format(formatter) +
                ", endTime=" + (endTime != null ? endTime.format(formatter) : "PENDING") +
                '}';
    }
}
