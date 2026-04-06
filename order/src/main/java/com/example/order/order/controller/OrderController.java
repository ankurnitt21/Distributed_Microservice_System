package com.example.order.order.controller;

import com.example.order.order.dto.OrderRequest;
import com.example.order.order.dto.OrderResponse;
import com.example.order.order.dto.PagedResponse;
import com.example.order.order.service.OrderService;
import com.example.order.order.service.OrderAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Tag(name = "Order API", description = "API for managing orders")
@RestController
@RequestMapping("/v1/orders")
public class OrderController {

    private OrderService orderService;
    private ObjectProvider<OrderAnalyticsService> analyticsServiceProvider;

    public OrderController(OrderService orderService, ObjectProvider<OrderAnalyticsService> analyticsServiceProvider) {
        this.orderService = orderService;
        this.analyticsServiceProvider = analyticsServiceProvider;
    }

    @Operation(summary = "Create a new order", description = "Creates a new order with the provided details")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        OrderAnalyticsService analytics = analyticsServiceProvider.getIfAvailable();
        if (analytics != null) {
            analytics.recordOrderCreated(orderResponse.getId(), orderResponse.getName(), orderResponse.getPrice());
        }

        return ResponseEntity.ok(orderResponse);
    }

    @Operation(summary = "Get all orders", description = "Retrieves all orders with optional name filter")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(orderService.getAllOrders(name));
    }

    // Pagination endpoint
    @Operation(summary = "Get orders with pagination")
    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<OrderResponse>> getOrdersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrdersPaged(page, size));
    }

    // @Async endpoint
    @Operation(summary = "Get all orders asynchronously")
    @GetMapping("/async")
    public CompletableFuture<ResponseEntity<List<OrderResponse>>> getAllOrdersAsync(
            @RequestParam(required = false) String name) {
        return orderService.getAllOrdersAsync(name)
                .thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Get analytics report")
    @GetMapping("/analytics/report")
    public ResponseEntity<OrderAnalyticsService.AnalyticsReport> getAnalyticsReport() {
        OrderAnalyticsService analytics = analyticsServiceProvider.getObject();
        return ResponseEntity.ok(analytics.generateReport());
    }

    @Operation(summary = "Get order statistics")
    @GetMapping("/{id}/analytics")
    public ResponseEntity<OrderAnalyticsService.OrderStats> getOrderAnalytics(@PathVariable Long id) {
        OrderAnalyticsService analytics = analyticsServiceProvider.getObject();
        OrderAnalyticsService.OrderStats stats = analytics.getOrderStats(id);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }
}

// API Versioning: v2 controller with different behavior
@Tag(name = "Order API v2", description = "API v2 with pagination by default")
@RestController
@RequestMapping("/v2/orders")
class OrderControllerV2 {

    private final OrderService orderService;

    public OrderControllerV2(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrdersPaged(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
