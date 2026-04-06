package com.example.order.order.Exceptions;

public class ServiceFallbackException extends RuntimeException {
    private final String serviceName;

    public ServiceFallbackException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
