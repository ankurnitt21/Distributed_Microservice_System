package com.example.product.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private double price;
    private String orderName;
    private Long timestamp;
}
