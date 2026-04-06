package com.example.product.product.dto;

import lombok.Data;

@Data
public class ProductResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final double price;
    private final Integer stock;
    private final String status;

    public ProductResponse(Long id, String name, String description, Double price, Integer stock, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }
}

