package com.example.product.product.Exceptions;

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
