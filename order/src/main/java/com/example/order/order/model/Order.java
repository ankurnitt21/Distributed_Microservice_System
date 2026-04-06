package com.example.order.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Order {
    private Long id;
    private Long productId;
    private String name;
    private String description;
    private double price;
    private Integer quantity;
    private String status;
}
