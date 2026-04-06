package com.example.product.product.service;

import com.example.product.product.Exceptions.ProductNotFoundException;
import com.example.product.product.Exceptions.ServiceFallbackException;
import com.example.product.product.dto.*;
import com.example.product.product.model.Product;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final Map<Long, Product> products = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_SUCCESS_TOPIC = "order-success";
    private static final String ORDER_FAILED_TOPIC = "order-failed";

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("Creating product {}", productRequest.getName());
        Product product = new Product();
        product.setId(nextId.getAndIncrement());
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        product.setStock(productRequest.getQuantity());

        products.put(product.getId(), product);
        return mapToResponse(product);
    }

    // ==================== Resilience4j: Bulkhead ====================

    @Bulkhead(name = "productApi")
    @Cacheable(value = "productById", key = "#id")
    public ProductResponse getProductById(Long id) {
        log.info("Getting product {}", id);
        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        return mapToResponse(product);
    }

    // ==================== Resilience4j: RateLimiter ====================

    @RateLimiter(name = "productApi", fallbackMethod = "rateLimitFallback")
    @Cacheable(value = "products", key = "'all-' + #name")
    public List<ProductResponse> getAllProducts(String name) {
        log.info("Getting all products");
        return products.values().stream()
                .filter(product -> name == null || product.getName().contains(name))
                .map(this::mapToResponse)
                .toList();
    }

    private List<ProductResponse> rateLimitFallback(String name, Throwable t) {
        throw new ServiceFallbackException("ProductService", "Rate limit exceeded. Try again later.");
    }

    // ==================== Pagination ====================

    public PagedResponse<ProductResponse> getProductsPaged(int page, int size) {
        List<ProductResponse> allProducts = products.values().stream()
                .map(this::mapToResponse)
                .toList();

        int totalElements = allProducts.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<ProductResponse> pageContent = allProducts.subList(fromIndex, toIndex);
        return new PagedResponse<>(pageContent, page, size, totalElements, totalPages);
    }

    // ==================== @Async ====================

    @Async("productTaskExecutor")
    public CompletableFuture<List<ProductResponse>> getAllProductsAsync(String name) {
        log.info("Async: fetching products on thread {}", Thread.currentThread().getName());
        List<ProductResponse> result = products.values().stream()
                .filter(product -> name == null || product.getName().contains(name))
                .map(this::mapToResponse)
                .toList();
        return CompletableFuture.completedFuture(result);
    }

    // ==================== @Scheduled ====================

    @Scheduled(fixedRateString = "${product.service.scheduling-rate-ms:60000}")
    public void logProductStats() {
        long lowStock = products.values().stream().filter(p -> p.getStock() != null && p.getStock() < 5).count();
        log.info("[SCHEDULED] Products stats — total={}, lowStock={}", products.size(), lowStock);
    }

    // ==================== Kafka: Saga Listener ====================

    @CircuitBreaker(name = "kafkaProcessor", fallbackMethod = "kafkaProcessorFallback")
    @KafkaListener(topics = "order-created", groupId = "product-service-group")
    @CacheEvict(value = {"products", "productById"}, allEntries = true)
    public void handleOrderCreated(OrderEvent orderEvent) {
        log.info("Received order-created event: orderId={}, productId={}, quantity={}",
                orderEvent.getOrderId(), orderEvent.getProductId(), orderEvent.getQuantity());

        Product product = products.get(orderEvent.getProductId());

        if (product == null) {
            log.warn("Product not found for productId: {}", orderEvent.getProductId());
            sendOrderFailedEvent(orderEvent, "Product not found with id: " + orderEvent.getProductId());
            return;
        }

        if (product.getStock() >= orderEvent.getQuantity()) {
            product.setStock(product.getStock() - orderEvent.getQuantity());
            log.info("Stock reduced: productId={}, stock_after={}", product.getId(), product.getStock());
            sendOrderSuccessEvent(orderEvent, product);
        } else {
            log.warn("Insufficient stock: available={}, required={}",
                    product.getStock(), orderEvent.getQuantity());
            sendOrderFailedEvent(orderEvent, "Insufficient stock. Available: " + product.getStock()
                    + ", Required: " + orderEvent.getQuantity());
        }
    }

    private void kafkaProcessorFallback(OrderEvent orderEvent, Throwable t) {
        log.error("Kafka processor fallback for orderId={}: {}", orderEvent.getOrderId(), t.getMessage());
        sendOrderFailedEvent(orderEvent, "Product Service processing failed: " + t.getMessage());
    }

    private void sendOrderSuccessEvent(OrderEvent orderEvent, Product product) {
        OrderSuccessEvent successEvent = new OrderSuccessEvent();
        successEvent.setOrderId(orderEvent.getOrderId());
        successEvent.setProductId(orderEvent.getProductId());
        successEvent.setQuantity(orderEvent.getQuantity());
        successEvent.setStatus("SUCCESS");
        successEvent.setTimestamp(System.currentTimeMillis());
        successEvent.setMessage("Stock confirmed and reduced");

        kafkaTemplate.send(ORDER_SUCCESS_TOPIC, String.valueOf(orderEvent.getOrderId()), successEvent);
        log.info("Kafka Event Sent: topic={}, orderId={}, status=SUCCESS", ORDER_SUCCESS_TOPIC, orderEvent.getOrderId());
    }

    private void sendOrderFailedEvent(OrderEvent orderEvent, String reason) {
        OrderFailedEvent failedEvent = new OrderFailedEvent();
        failedEvent.setOrderId(orderEvent.getOrderId());
        failedEvent.setProductId(orderEvent.getProductId());
        failedEvent.setQuantity(orderEvent.getQuantity());
        failedEvent.setStatus("FAILED");
        failedEvent.setTimestamp(System.currentTimeMillis());
        failedEvent.setFailureReason(reason);

        kafkaTemplate.send(ORDER_FAILED_TOPIC, String.valueOf(orderEvent.getOrderId()), failedEvent);
        log.info("Kafka Event Sent: topic={}, orderId={}, status=FAILED, reason={}",
                ORDER_FAILED_TOPIC, orderEvent.getOrderId(), reason);
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                "AVAILABLE"
        );
    }
}
