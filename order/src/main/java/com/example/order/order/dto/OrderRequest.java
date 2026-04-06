package com.example.order.order.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull
    private Long productId;

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be at least 0.0")
    private double price;

    @Min(1)
    private Integer quantity;
}
