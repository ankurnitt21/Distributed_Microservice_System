package com.example.order.order;

import com.example.order.order.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableCaching
@EnableScheduling
public class OrderApplication {

	private static final Logger logger = LoggerFactory.getLogger(OrderApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(OrderApplication.class, args);
		AppConfig appConfig = context.getBean(AppConfig.class);
		logger.info("Application started with configuration: {}", appConfig);
	}

}
