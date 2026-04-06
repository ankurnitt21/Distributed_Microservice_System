package com.example.product.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderFailedEvent {
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String status;
    private Long timestamp;
    private String failureReason;
}
