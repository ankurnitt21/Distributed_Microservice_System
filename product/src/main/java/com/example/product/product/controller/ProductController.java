package com.example.product.product.controller;

import com.example.product.product.config.ProductReportGenerator;
import com.example.product.product.dto.PagedResponse;
import com.example.product.product.dto.ProductRequest;
import com.example.product.product.dto.ProductResponse;
import com.example.product.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Tag(name = "Product API", description = "API for managing products")
@RestController
@RequestMapping("/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductReportGenerator reportGenerator;

    public ProductController(ProductService productService, ProductReportGenerator reportGenerator) {
        this.productService = productService;
        this.reportGenerator = reportGenerator;
    }

    @Operation(summary = "Create a new product")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        return ResponseEntity.ok(productService.createProduct(productRequest));
    }

    @Operation(summary = "Get all products")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(productService.getAllProducts(name));
    }

    // Pagination endpoint
    @Operation(summary = "Get products with pagination")
    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<ProductResponse>> getProductsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getProductsPaged(page, size));
    }

    // @Async endpoint
    @Operation(summary = "Get all products asynchronously")
    @GetMapping("/async")
    public CompletableFuture<ResponseEntity<List<ProductResponse>>> getAllProductsAsync(
            @RequestParam(required = false) String name) {
        return productService.getAllProductsAsync(name)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Generate product report")
    @PostMapping("/{id}/report")
    public ResponseEntity<String> generateProductReport(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        String report = reportGenerator.generateProductReport(
                id.toString(), product.getName(), product.getPrice());
        return ResponseEntity.ok(report);
    }
}

// API Versioning: v2 controller with pagination by default
@Tag(name = "Product API v2", description = "API v2 with pagination by default")
@RestController
@RequestMapping("/v2/products")
class ProductControllerV2 {

    private final ProductService productService;

    public ProductControllerV2(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getProductsPaged(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
