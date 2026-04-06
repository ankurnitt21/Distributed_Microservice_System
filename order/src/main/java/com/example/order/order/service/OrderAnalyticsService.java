package com.example.order.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Lazy
public class OrderAnalyticsService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderAnalyticsService.class);
    
    private Map<Long, OrderStats> stats;
    private AtomicLong totalOrdersProcessed;
    
    @PostConstruct
    public void init() {
        log.info("★ OrderAnalyticsService initialization started (EXPENSIVE OPERATION)");
        
        // Simulate expensive initialization (e.g., connecting to external analytics API)
        try {
            Thread.sleep(2000);  // Simulate heavy I/O (DB connection, API call, ML model loading)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        this.stats = new HashMap<>();
        this.totalOrdersProcessed = new AtomicLong(0);
        
        log.info("★ OrderAnalyticsService initialized successfully (initialized only when first used due to @Lazy)");
    }
    
    /**
     * Record order analytics data
     * Called only when analytics are needed (not on every request)
     */
    public void recordOrderCreated(Long orderId, String orderName, double price) {
        if (stats == null) {
            throw new RuntimeException("OrderAnalyticsService not initialized");
        }
        
        log.info("📊 Recording analytics: Order {} - {} (₹{})", orderId, orderName, price);
        
        OrderStats stat = stats.getOrDefault(orderId, new OrderStats());
        stat.setOrderId(orderId);
        stat.setOrderName(orderName);
        stat.setPrice(price);
        stat.setTimestamp(System.currentTimeMillis());
        
        stats.put(orderId, stat);
        totalOrdersProcessed.incrementAndGet();
    }
    
    /**
     * Generate analytics report
     * Expensive operation - only executed when explicitly requested
     */
    public AnalyticsReport generateReport() {
        if (stats == null) {
            throw new RuntimeException("OrderAnalyticsService not initialized");
        }
        
        log.info("📈 Generating analytics report from {} orders", stats.size());
        
        AnalyticsReport report = new AnalyticsReport();
        report.setTotalOrders(stats.size());
        report.setTotalProcessed(totalOrdersProcessed.get());
        
        double totalRevenue = stats.values().stream()
                .mapToDouble(OrderStats::getPrice)
                .sum();
        report.setTotalRevenue(totalRevenue);
        
        double avgPrice = stats.values().stream()
                .mapToDouble(OrderStats::getPrice)
                .average()
                .orElse(0.0);
        report.setAveragePrice(avgPrice);
        
        return report;
    }
    
    /**
     * Get analytics for specific order
     */
    public OrderStats getOrderStats(Long orderId) {
        if (stats == null) {
            throw new RuntimeException("OrderAnalyticsService not initialized");
        }
        return stats.getOrDefault(orderId, null);
    }
    
    // Inner classes
    public static class OrderStats {
        private Long orderId;
        private String orderName;
        private double price;
        private long timestamp;
        
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public String getOrderName() { return orderName; }
        public void setOrderName(String orderName) { this.orderName = orderName; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    public static class AnalyticsReport {
        private long totalOrders;
        private long totalProcessed;
        private double totalRevenue;
        private double averagePrice;
        
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        
        public long getTotalProcessed() { return totalProcessed; }
        public void setTotalProcessed(long totalProcessed) { this.totalProcessed = totalProcessed; }
        
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public double getAveragePrice() { return averagePrice; }
        public void setAveragePrice(double averagePrice) { this.averagePrice = averagePrice; }
        
        @Override
        public String toString() {
            return "AnalyticsReport{" +
                    "totalOrders=" + totalOrders +
                    ", totalProcessed=" + totalProcessed +
                    ", totalRevenue=" + totalRevenue +
                    ", averagePrice=" + averagePrice +
                    '}';
        }
    }
}
