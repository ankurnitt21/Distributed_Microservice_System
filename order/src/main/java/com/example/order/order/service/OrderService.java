package com.example.order.order.service;

import com.example.order.order.Exceptions.OrderNotFoundException;
import com.example.order.order.Exceptions.ProductServiceException;
import com.example.order.order.Exceptions.ServiceFallbackException;
import com.example.order.order.config.OrderServiceProperties;
import com.example.order.order.dto.*;
import com.example.order.order.model.Order;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final Map<Long, Order> orders = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderServiceProperties properties;

    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String ORDER_SUCCESS_TOPIC = "order-success";
    private static final String ORDER_FAILED_TOPIC = "order-failed";

    // ==================== Resilience4j: CircuitBreaker + Retry ====================

    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating order {} with productId={}", orderRequest.getName(), orderRequest.getProductId());

        ProductResponse product = getProductFromService(orderRequest.getProductId());
        log.info("Product found: id={}, price={}, stock={}", product.getId(), product.getPrice(), product.getStock());

        double finalPrice = orderRequest.getPrice() > 0 ? orderRequest.getPrice() : product.getPrice();

        Order order = new Order();
        order.setId(nextId.getAndIncrement());
        order.setProductId(orderRequest.getProductId());
        order.setName(orderRequest.getName());
        order.setDescription(orderRequest.getDescription());
        order.setPrice(finalPrice);
        order.setQuantity(orderRequest.getQuantity());
        order.setStatus("PENDING");

        orders.put(order.getId(), order);
        log.info("Order created with id={}, status=PENDING", order.getId());

        sendOrderCreatedEvent(order, product);
        return mapToResponse(order);
    }

    // CircuitBreaker + Retry on Product Service call
    @CircuitBreaker(name = "productService", fallbackMethod = "productServiceFallback")
    @Retry(name = "productService")
    private ProductResponse getProductFromService(Long productId) {
        log.info("Calling Product Service for productId: {}", productId);
        ProductResponse product = restTemplate.getForObject(
                properties.getProductServiceUrl() + productId,
                ProductResponse.class
        );
        if (product == null) {
            throw new ProductServiceException("Product not found with id: " + productId);
        }
        return product;
    }

    // Fallback with custom exception
    private ProductResponse productServiceFallback(Long productId, Throwable t) {
        log.error("Product Service fallback triggered for productId={}: {}", productId, t.getMessage());
        throw new ServiceFallbackException("ProductService",
                "Product Service is unavailable for productId: " + productId + ". Reason: " + t.getMessage());
    }

    // ==================== Resilience4j: RateLimiter ====================

    @RateLimiter(name = "orderApi", fallbackMethod = "rateLimitFallback")
    @Cacheable(value = "orders", key = "'all-' + #name")
    public List<OrderResponse> getAllOrders(String name) {
        log.info("Getting all orders with filter={}", name);
        return orders.values().stream()
                .filter(order -> name == null || order.getName().contains(name))
                .map(this::mapToResponse)
                .toList();
    }

    private List<OrderResponse> rateLimitFallback(String name, Throwable t) {
        throw new ServiceFallbackException("OrderService", "Rate limit exceeded. Try again later.");
    }

    // ==================== Resilience4j: Bulkhead ====================

    @Bulkhead(name = "orderApi")
    @Cacheable(value = "orderById", key = "#id")
    public OrderResponse getOrderById(Long id) {
        log.info("Getting order {}", id);
        Order order = orders.get(id);
        if (order == null) {
            throw new OrderNotFoundException("Order not found with id: " + id);
        }
        return mapToResponse(order);
    }

    // ==================== Pagination ====================

    public PagedResponse<OrderResponse> getOrdersPaged(int page, int size) {
        List<OrderResponse> allOrders = orders.values().stream()
                .map(this::mapToResponse)
                .toList();

        int totalElements = allOrders.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<OrderResponse> pageContent = allOrders.subList(fromIndex, toIndex);
        return new PagedResponse<>(pageContent, page, size, totalElements, totalPages);
    }

    // ==================== @Async ====================

    @Async("orderTaskExecutor")
    public CompletableFuture<List<OrderResponse>> getAllOrdersAsync(String name) {
        log.info("Async: fetching orders on thread {}", Thread.currentThread().getName());
        List<OrderResponse> result = orders.values().stream()
                .filter(order -> name == null || order.getName().contains(name))
                .map(this::mapToResponse)
                .toList();
        return CompletableFuture.completedFuture(result);
    }

    // ==================== @Scheduled ====================

    @Scheduled(fixedRateString = "${order.service.scheduling-rate-ms:60000}")
    public void logOrderStats() {
        long pending = orders.values().stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        long confirmed = orders.values().stream().filter(o -> "CONFIRMED".equals(o.getStatus())).count();
        long failed = orders.values().stream().filter(o -> "FAILED".equals(o.getStatus())).count();
        log.info("[SCHEDULED] Orders stats — total={}, pending={}, confirmed={}, failed={}",
                orders.size(), pending, confirmed, failed);
    }

    // ==================== Kafka ====================

    private void sendOrderCreatedEvent(Order order, ProductResponse product) {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(order.getId());
        orderEvent.setProductId(product.getId());
        orderEvent.setQuantity(order.getQuantity());
        orderEvent.setPrice(order.getPrice());
        orderEvent.setOrderName(order.getName());
        orderEvent.setTimestamp(System.currentTimeMillis());

        kafkaTemplate.send(ORDER_CREATED_TOPIC, String.valueOf(order.getId()), orderEvent);
        log.info("Kafka Event Sent: topic={}, orderId={}, productId={}, quantity={}",
                ORDER_CREATED_TOPIC, order.getId(), product.getId(), order.getQuantity());
    }

    @KafkaListener(topics = ORDER_SUCCESS_TOPIC, groupId = "order-service-group")
    @CacheEvict(value = {"orders", "orderById"}, allEntries = true)
    public void handleOrderSuccess(OrderSuccessEvent successEvent) {
        log.info("SUCCESS Event Received: orderId={}, status={}",
                successEvent.getOrderId(), successEvent.getStatus());
        Order order = orders.get(successEvent.getOrderId());
        if (order != null) {
            order.setStatus("CONFIRMED");
            log.info("Order status updated to CONFIRMED: orderId={}", successEvent.getOrderId());
        }
    }

    @KafkaListener(topics = ORDER_FAILED_TOPIC, groupId = "order-service-group")
    @CacheEvict(value = {"orders", "orderById"}, allEntries = true)
    public void handleOrderFailed(OrderFailedEvent failedEvent) {
        log.info("FAILED Event Received: orderId={}, reason={}",
                failedEvent.getOrderId(), failedEvent.getFailureReason());
        Order order = orders.get(failedEvent.getOrderId());
        if (order != null) {
            order.setStatus("FAILED");
            log.info("Order status updated to FAILED: orderId={}", failedEvent.getOrderId());
        }
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setProductId(order.getProductId());
        response.setName(order.getName());
        response.setDescription(order.getDescription());
        response.setPrice(order.getPrice());
        response.setStatus(order.getStatus());
        return response;
    }
}
