package com.example.product.product.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    private final ObjectFactory<RequestTimingContext> timingContextFactory;

    public LoggingInterceptor(ObjectFactory<RequestTimingContext> timingContextFactory) {
        this.timingContextFactory = timingContextFactory;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String correlationId = request.getHeader("X-Correlation-ID");
        String employeeId = request.getHeader("X-Employee-ID");

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = java.util.UUID.randomUUID().toString();
        }
        if (employeeId == null || employeeId.isEmpty()) {
            employeeId = "Unknown";
        }

        MDC.put("correlationId", correlationId);
        MDC.put("employeeId", employeeId != null ? employeeId : "Unknown");
        
        // Get prototype-scoped request timing context (new instance per request)
        RequestTimingContext timingContext = timingContextFactory.getObject();
        request.setAttribute("timingContext", timingContext);
        
        log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
        timingContext.logStartTime();
        request.setAttribute(START_TIME, System.currentTimeMillis());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        long startTime = (long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - startTime;

        String correlationId = request.getHeader("X-Correlation-ID");
        String employeeId = request.getHeader("X-Employee-ID");

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = "N/A";
        }
        
        // Get the timing context and log end time
        RequestTimingContext timingContext = (RequestTimingContext) request.getAttribute("timingContext");
        if (timingContext != null) {
            timingContext.logEndTime();
        }
        if (employeeId == null || employeeId.isEmpty()) {
            employeeId = "Unknown";
        }

        log.info("Request completed: CorrelationID={}, EmployeeID={}, Duration={} ms, ResponseStatus={}", correlationId, employeeId, duration, response.getStatus());
        MDC.clear();
    }
}
