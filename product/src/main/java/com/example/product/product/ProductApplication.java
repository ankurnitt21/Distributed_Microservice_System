package com.example.product.product;

import com.example.product.product.config.ApplicationProperties;
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
public class ProductApplication {

	private static final Logger logger = LoggerFactory.getLogger(ProductApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ProductApplication.class, args);
		ApplicationProperties appProps = context.getBean(ApplicationProperties.class);
		logger.info("Application started with configuration: {}", appProps);
	}

}
