package com.example.product.product.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Producer Factory Configuration
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        
        // Add Confluent Cloud SSL/SASL properties
        if (bootstrapServers.contains("confluent.cloud")) {
            configProps.put("security.protocol", "SASL_SSL");
            configProps.put("sasl.mechanism", "PLAIN");
            configProps.put("sasl.jaas.config", 
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"KOBXAXKO5FTSLBLB\" password=\"cfltI+IDXbYrU2mqj3wKwsTEpAfWQVXrPlZ9wPjKNKYjPtVkfPrZEJ538QKZAWSA\";");
        }
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka Template Bean
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Consumer Factory Configuration
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.product.product.dto.OrderEvent");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        // Add Confluent Cloud SSL/SASL properties
        if (bootstrapServers.contains("confluent.cloud")) {
            configProps.put("security.protocol", "SASL_SSL");
            configProps.put("sasl.mechanism", "PLAIN");
            configProps.put("sasl.jaas.config", 
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"KOBXAXKO5FTSLBLB\" password=\"cfltI+IDXbYrU2mqj3wKwsTEpAfWQVXrPlZ9wPjKNKYjPtVkfPrZEJ538QKZAWSA\";");
        }
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka Listener Container Factory
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
    }

    /**
     * Admin Factory for Topic Creation
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Add Confluent Cloud SSL/SASL properties
        if (bootstrapServers.contains("confluent.cloud")) {
            configs.put("security.protocol", "SASL_SSL");
            configs.put("sasl.mechanism", "PLAIN");
            configs.put("sasl.jaas.config", 
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"KOBXAXKO5FTSLBLB\" password=\"cfltI+IDXbYrU2mqj3wKwsTEpAfWQVXrPlZ9wPjKNKYjPtVkfPrZEJ538QKZAWSA\";");
        }
        
        return new KafkaAdmin(configs);
    }

    /**
     * Topic Beans - Auto-create topics if they don't exist
     */
    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("order-created")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic orderSuccessTopic() {
        return TopicBuilder.name("order-success")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic orderFailedTopic() {
        return TopicBuilder.name("order-failed")
                .partitions(3)
                .replicas(3)
                .build();
    }
}
