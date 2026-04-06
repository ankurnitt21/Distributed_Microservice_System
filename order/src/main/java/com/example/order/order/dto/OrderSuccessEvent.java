package com.example.order.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSuccessEvent {
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String status;
    private Long timestamp;
    private String message;
}
