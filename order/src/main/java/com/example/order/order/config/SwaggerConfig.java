package com.example.order.order.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("v1")
                        .description("API documentation for Order Service")
                )
                .components(new Components()
                        .addParameters("correlationIdHeader",
                                new Parameter()
                                        .in("header")
                                        .name("X-Correlation-Id")
                                        .description("Correlation ID for request tracing")
                                        .required(false)
                        )
                        .addParameters("employeeIdHeader",
                                new Parameter()
                                        .in("header")
                                        .name("X-Employee-Id")
                                        .description("Employee ID")
                                        .required(false)
                        )
                );
    }

    @Bean
    public OperationCustomizer customHeaders() {
        return (Operation operation, org.springframework.web.method.HandlerMethod handlerMethod) -> {

            Parameter correlationId = new Parameter()
                    .in("header")
                    .name("X-Correlation-Id")
                    .description("Correlation ID")
                    .required(false);

            Parameter employeeId = new Parameter()
                    .in("header")
                    .name("X-Employee-Id")
                    .description("Employee ID")
                    .required(false);

            operation.addParametersItem(correlationId);
            operation.addParametersItem(employeeId);

            return operation;
        };
    }
}
