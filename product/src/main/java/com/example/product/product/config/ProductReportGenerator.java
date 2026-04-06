package com.example.product.product.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Lazy
public class ProductReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ProductReportGenerator.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ConcurrentHashMap<String, ReportMetadata> reportCache;
    private boolean initialized;

    public ProductReportGenerator() {
        logger.info("[LAZY BEAN] ProductReportGenerator constructor called - FIRST TIME ACCESS");
        this.reportCache = new ConcurrentHashMap<>();
        this.initialized = true;
        
        // Simulate expensive initialization (e.g., loading templates, connecting to external services)
        try {
            Thread.sleep(2000); // Simulate 2 second initialization
            logger.info("[LAZY BEAN] ProductReportGenerator initialization complete - Cache ready");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String generateProductReport(String productId, String productName, double price) {
        if (!initialized) {
            throw new IllegalStateException("ReportGenerator not initialized");
        }
        
        String reportId = "REPORT-" + System.currentTimeMillis();
        ReportMetadata metadata = new ReportMetadata(reportId, productId, productName, price, LocalDateTime.now());
        reportCache.put(reportId, metadata);
        
        logger.info("[REPORT] Generated report {} for product {} | Price: {} | Cached reports: {}", 
            reportId, productName, price, reportCache.size());
        
        return String.format(
            "=== PRODUCT REPORT ===\n" +
            "Report ID: %s\n" +
            "Product ID: %s\n" +
            "Product Name: %s\n" +
            "Price: $%.2f\n" +
            "Generated: %s\n" +
            "Total Cached Reports: %d",
            reportId, productId, productName, price, LocalDateTime.now().format(formatter), reportCache.size()
        );
    }

    public int getCachedReportsCount() {
        return reportCache.size();
    }

    public ConcurrentHashMap<String, ReportMetadata> getReportCache() {
        return reportCache;
    }

    public static class ReportMetadata {
        private final String reportId;
        private final String productId;
        private final String productName;
        private final double price;
        private final LocalDateTime generatedAt;

        public ReportMetadata(String reportId, String productId, String productName, double price, LocalDateTime generatedAt) {
            this.reportId = reportId;
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.generatedAt = generatedAt;
        }

        public String getReportId() {
            return reportId;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }

        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }

        @Override
        public String toString() {
            return "ReportMetadata{" +
                    "reportId='" + reportId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", price=" + price +
                    ", generatedAt=" + generatedAt +
                    '}';
        }
    }
}
