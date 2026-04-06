package com.example.product.product.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

public interface NotificationService {
    void sendNotification(String message);
}

@Configuration
class NotificationConfig {

    @Bean
    @Profile("dev")
    public NotificationService consoleNotification() {
        return new ConsoleNotificationService();
    }

    @Bean
    @Profile("prod")
    public NotificationService emailNotification() {
        return new EmailNotificationService();
    }

    @Bean
    @ConditionalOnProperty(name = "product.service.cache-enabled", havingValue = "true", matchIfMissing = true)
    public String cacheEnabledMarker() {
        return "CACHE_ENABLED";
    }
}

class ConsoleNotificationService implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationService.class);

    @Override
    public void sendNotification(String message) {
        log.info("[CONSOLE] Notification: {}", message);
    }
}

class EmailNotificationService implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Override
    public void sendNotification(String message) {
        log.info("[EMAIL] Notification sent: {}", message);
    }
}
