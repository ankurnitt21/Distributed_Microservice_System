package com.example.order.order.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @DecimalMin(value = "100.0", inclusive = true, message = "Price must be at least 100.0")
    private double price;

    @Min(1)
    private Integer quantity;


}
