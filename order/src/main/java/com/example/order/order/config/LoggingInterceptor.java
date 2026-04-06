package com.example.order.order.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String PROCESSING_CONTEXT = "processingContext";
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Autowired
    private ObjectProvider<OrderProcessingContext> contextProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get fresh prototype instance per request
        OrderProcessingContext context = contextProvider.getObject();
        context.setStartTime(System.currentTimeMillis());
        
        // Store context in request attribute for later retrieval
        request.setAttribute(PROCESSING_CONTEXT, context);

        // Handle correlation and employee ID via MDC
        String correlationId = request.getHeader("X-Correlation-ID");
        String employeeId = request.getHeader("X-Employee-ID");

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = java.util.UUID.randomUUID().toString();
        }
        if (employeeId == null || employeeId.isEmpty()) {
            employeeId = "Unknown";
        }

        MDC.put("correlationId", correlationId);
        MDC.put("employeeId", employeeId);
        log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // Retrieve the context from request attribute
        OrderProcessingContext context = (OrderProcessingContext) request.getAttribute(PROCESSING_CONTEXT);
        
        long duration = 0;
        if (context != null) {
            duration = System.currentTimeMillis() - context.getStartTime();
            context.setDuration(duration);
        }

        // CorrelationID and EmployeeID are in MDC and will appear via logback pattern
        log.info("Request completed: Duration={} ms, ResponseStatus={}", duration, response.getStatus());
        MDC.clear();
    }
}
