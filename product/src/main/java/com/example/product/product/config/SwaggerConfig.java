package com.example.product.product.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Unified Swagger/OpenAPI Configuration
 * 
 * This configuration provides:
 * 1. API documentation with title, version, and description
 * 2. Global header parameters (correlationId, employeeId) added to ALL endpoints
 * 3. Reusable parameter definitions in OpenAPI schema
 */
@Configuration
public class SwaggerConfig {

    /**
     * Define OpenAPI schema with API info and reusable parameter definitions
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .version("v1")
                        .description("Advanced Spring Boot API with Multi-Profile Configuration, " +
                                "Prototype Scope, @Lazy Beans, Request Timing, and Global Exception Handling")
                )
                .components(new Components()
                        .addParameters("correlationIdHeader",
                                new Parameter()
                                        .in("header")
                                        .name("X-Correlation-Id")
                                        .description("Unique correlation ID for distributed tracing across services")
                                        .required(false)
                        )
                        .addParameters("employeeIdHeader",
                                new Parameter()
                                        .in("header")
                                        .name("X-Employee-Id")
                                        .description("Employee/User ID for audit logging and request tracking")
                                        .required(false)
                        )
                );
    }

    /**
     * Customize operation (endpoint) to add global headers
     * This bean is used by Springdoc to process each endpoint definition
     */
    @Bean
    public OperationCustomizer customHeaders() {
        return (Operation operation, org.springframework.web.method.HandlerMethod handlerMethod) -> {
            
            // Define correlation header parameter
            Parameter correlationId = new Parameter()
                    .in("header")
                    .name("X-Correlation-Id")
                    .description("Correlation ID for request tracing and MDC propagation")
                    .required(false)
                    .schema(new io.swagger.v3.oas.models.media.Schema<>().type("string"));

            // Define employee header parameter
            Parameter employeeId = new Parameter()
                    .in("header")
                    .name("X-Employee-Id")
                    .description("Employee ID for audit logging")
                    .required(false)
                    .schema(new io.swagger.v3.oas.models.media.Schema<>().type("string"));

            // Add both headers to every endpoint automatically
            operation.addParametersItem(correlationId);
            operation.addParametersItem(employeeId);

            return operation;
        };
    }
}