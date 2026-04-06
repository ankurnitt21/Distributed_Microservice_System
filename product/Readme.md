# Product Service - Spring Boot Microservice

## Table of Contents

| # | Topic |
|---|-------|
| 1 | [Overview](#1-overview) |
| 2 | [Tech Stack](#2-tech-stack) |
| 3 | [Project Structure](#3-project-structure) |
| 4 | [How to Run](#4-how-to-run) |
| 5 | [REST API Endpoints](#5-rest-api-endpoints) |
| 6 | [Configuration and Profiles](#6-configuration-and-profiles) |
| 7 | [@ConfigurationProperties](#7-configurationproperties---type-safe-config) |
| 8 | [Spring IoC and Request Isolation](#8-spring-ioc-and-request-isolation) |
| 9 | [@Lazy Annotation](#9-lazy-annotation---deferred-bean-initialization) |
| 10 | [Bean Scoping - Prototype for Multi-Threading](#10-bean-scoping---prototype-for-multi-threading) |
| 11 | [Spring Bean Lifecycle](#11-spring-bean-lifecycle) |
| 12 | [BeanFactory and ApplicationContext](#12-beanfactory-and-applicationcontext) |
| 13 | [BeanFactoryPostProcessor](#13-beanfactorypostprocessor) |
| 14 | [Validation and Error Handling](#14-validation-and-error-handling) |
| 15 | [Logging and MDC](#15-logging-and-mdc) |
| 16 | [MDC Propagation Across Threads](#16-mdc-propagation-across-threads) |
| 17 | [Swagger/OpenAPI Documentation](#17-swaggeropenapi-documentation) |
| 18 | [@Conditional + @Profile + @Qualifier](#18-conditional--profile--qualifier) |
| 19 | [Spring Boot DevTools](#19-spring-boot-devtools) |
| 20 | [Spring Boot Actuator](#20-spring-boot-actuator) |
| 21 | [Saga Pattern with Kafka](#21-saga-pattern-with-kafka) |
| 22 | [Kafka Event Rollback](#22-kafka-event-rollback) |
| 23 | [Confluent Cloud Kafka Setup](#23-confluent-cloud-kafka-setup) |
| 24 | [RestTemplate - Sync Communication](#24-resttemplate---sync-communication) |
| 25 | [Resilience4j - Fault Tolerance](#25-resilience4j---fault-tolerance) |
| 26 | [@Async - Asynchronous Processing](#26-async---asynchronous-processing) |
| 27 | [@Cacheable - Spring Cache](#27-cacheable---spring-cache) |
| 28 | [@Scheduled - Task Scheduling](#28-scheduled---task-scheduling) |
| 29 | [API Versioning](#29-api-versioning) |
| 30 | [Pagination](#30-pagination) |
| 31 | [Dockerization](#31-dockerization) |
| 32 | [Testing and Troubleshooting](#32-testing-and-troubleshooting) |
| 33 | [Future Enhancements](#33-future-enhancements) |

---


## 1. Overview

This is a **Spring Boot Product Microservice** built with production-grade features including:

✅ **Multi-Profile Configuration** (dev, prod, test)
✅ **@Value Annotation** for property binding
✅ **Prototype Scope** for request isolation (RequestTimingContext)
✅ **@Lazy Annotation** for deferred bean initialization
✅ **Spring Boot Actuator** for monitoring & health checks
✅ **Global Exception Handling**
✅ **Swagger/OpenAPI Documentation**
✅ **Structured Logging** with interceptors
✅ **Request Parameter Filtering**
✅ **Clean Architecture** principles
✅ **Plain POJO Models** (no unnecessary Spring beans)

---

## 2. Tech Stack

| Component | Technology |
|-----------|-----------|
| **Framework** | Spring Boot 3.x |
| **Language** | Java 17+ |
| **Build Tool** | Maven |
| **API Documentation** | Springdoc OpenAPI 2.0 + Swagger UI |
| **Monitoring** | Spring Boot Actuator (Health, Metrics, Probes) |
| **Validation** | Jakarta Validation |
| **Logging** | SLF4J + Logback |
| **Utilities** | Lombok (Java) |
| **HTTP Interceptor** | Custom LoggingInterceptor |
| **Development Tools** | Spring Boot DevTools (Auto-restart, Live Reload) |

---

## 3. Project Structure

```
src/main/
├── java/com/example/product/product/
│   ├── ProductApplication.java
│   │
│   ├── controller/
│   │   └── ProductController.java
│   │       ├── POST /v1/products
│   │       ├── GET /v1/products (with ?name filter)
│   │       ├── GET /v1/products/{id}
│   │       └── POST /v1/products/{id}/report (uses @Lazy)
│   │
│   ├── service/
│   │   └── ProductService.java
│   │       ├── createProduct()
│   │       ├── getProductById()
│   │       └── getAllProducts()
│   │
│   ├── model/
│   │   └── Product.java (Plain POJO)
│   │
│   ├── dto/
│   │   ├── ProductRequest.java (with @Valid)
│   │   ├── ProductResponse.java
│   │   └── ErrorResponse.java
│   │
│   ├── config/
│   │   ├── ApplicationProperties.java (@Value bindings)
│   │   │   ├── app.environment
│   │   │   ├── app.api.timeout
│   │   │   └── server.port
│   │   │
│   │   ├── RequestTimingContext.java (@Scope(PROTOTYPE))
│   │   │   ├── requestId (UUID)
│   │   │   ├── startTime (per-request)
│   │   │   ├── endTime (per-request)
│   │   │   ├── logStartTime()
│   │   │   └── logEndTime()
│   │   │
│   │   ├── ProductReportGenerator.java (@Lazy - Deferred Initialization)
│   │   │   ├── ConcurrentHashMap for report cache
│   │   │   ├── generateProductReport()
│   │   │   └── Expensive initialization (simulated 2s delay)
│   │   │
│   │   ├── LoggingInterceptor.java (Uses RequestTimingContext)
│   │   ├── SwaggerConfig.java (Merged: OpenAPI + Global Headers)
│   │   └── WebConfig.java
│   │
│   └── Exceptions/
│       ├── GlobalExceptionHandler.java
│       └── ProductNotFoundException.java
│
└── resources/
    ├── application.properties (main config, default prof ile=dev)
    ├── application-dev.properties (Development)
    ├── application-prod.properties (Production)
    ├── application-test.properties (Testing)
    └── logback-spring.xml (Logging config)
```

---

## 4. How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build Project
```bash
mvn clean package
```

### Run with Maven (dev profile by default)
```bash
mvn spring-boot:run
```

### Run with Maven (specific profile)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Run JAR File
```bash
java -jar target/product-*.jar --spring.profiles.active=prod
```

### Expected Output
```
Application started with configuration: ApplicationProperties{
  environment='development', 
  apiTimeout=5000, 
  serverPort=8081
}
```

---

## 5. REST API Endpoints

### 1. Create Product
```http
POST /v1/products HTTP/1.1
Content-Type: application/json

{
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "quantity": 10
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "quantity": 10
}
```

### 2. Get All Products (with optional filter)
```http
GET /v1/products?name=Laptop HTTP/1.1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "price": 1299.99,
    ...
  }
]
```

### 3. Get Product by ID
```http
GET /v1/products/1 HTTP/1.1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "quantity": 10
}
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Product not found with id: 999",
  "timestamp": "2026-04-05T10:30:45"
}
```

### 4. Generate Product Report (Uses @Lazy Bean)
```http
POST /v1/products/1/report HTTP/1.1
```

**Response (200 OK) - First Request (Triggers Lazy Initialization):**
```
=== PRODUCT REPORT ===
Report ID: REPORT-1712325045123
Product ID: 1
Product Name: Laptop
Price: $1299.99
Generated: 2026-04-05 10:30:45
Total Cached Reports: 1
```

**⏱️ First Request Timeline:**
```
Request arrives
Call reportGenerator (FIRST TIME - triggers @Lazy initialization)
Constructor runs (2 second delay)
[LAZY BEAN] ProductReportGenerator - Created on FIRST ACCESS
[LAZY BEAN] ProductReportGenerator ready
Generate report
Total time: ~2.1 seconds
```

**🚀 Subsequent Requests (No Initialization Delay):**
```
Request arrives
reportGenerator already initialized ✅
Generate report immediately
Total time: ~100ms
```

**Log Example:**
```
INFO  ProductReportGenerator - [LAZY BEAN] ProductReportGenerator - Created on FIRST ACCESS
INFO  ProductReportGenerator - [LAZY BEAN] ProductReportGenerator ready
INFO  ProductReportGenerator - [REPORT] Generated REPORT-1712325045123 for Laptop
```

### Request Parameter Filtering

### Filter by Product Name
```bash
curl "http://localhost:8081/v1/products?name=laptop"
```

**How it works:**
- `?name=laptop` - Optional query parameter
- Filters products by name (case-sensitive contains match)
- If not provided, returns all products

---

## 6. Configuration and Profiles

### Profile Comparison

| Feature | Dev | Prod | Test |
|---------|-----|------|------|
| **Port** | 8081 | 8080 | 8082 |
| **Log Level** | DEBUG | INFO | WARN |
| **App Name** | product-dev | product-prod | product-test |
| **DB Mode** | update | validate | create-drop |
| **API Timeout** | 5s | 30s | 10s |
| **Show SQL** | ✅ true | ❌ false | ❌ false |

### Set Active Profile

**In application.properties:**
```properties
spring.profiles.active=dev
```

**Via Environment Variable:**
```bash
export SPRING_PROFILES_ACTIVE=prod
```

**Via Command Line:**
```bash
java -jar app.jar --spring.profiles.active=prod
```

**Via Maven:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=test"
```

---

## 🔌 @Value Annotation & ApplicationProperties

Located in: `config/ApplicationProperties.java`

```java
@Component
public class ApplicationProperties {
    @Value("${app.environment}")
    private String environment;
    
    @Value("${app.api.timeout:5000}")
    private long apiTimeout;
    
    @Value("${server.port:8080}")
    private Integer serverPort;
}
```

**Features:**
- Environment-specific property injection
- Default values using colon syntax (`:`)
- Centralized configuration management
- Injected at startup in `ProductApplication.java` with logging

---

## 7. @ConfigurationProperties - Type-Safe Config

```java
@Data @Component
@ConfigurationProperties(prefix = "product.service")
public class ProductServiceProperties {
    private int pageSize = 10;
    private boolean asyncEnabled = true;
    private boolean cacheEnabled = true;
    private long schedulingRateMs = 60000;
}
```

---

## 8. Spring IoC and Request Isolation

### What is Prototype Scope?

Unlike Singleton (one instance for whole app), **Prototype** creates:
- **New instance per request** ✅
- **No shared state** ✅
- **Request isolation** ✅

## ⏱️ Request Timing with Prototype Scope

### RequestTimingContext Implementation

Located in: `config/RequestTimingContext.java`

Each HTTP request gets its own **isolated timing context**:

```java
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RequestTimingContext {
    private final String requestId = UUID.randomUUID().toString();
    private final LocalDateTime startTime = LocalDateTime.now();
    
    public void logStartTime() {
        // Logs: [REQUEST START] RequestID: {uuid} | Time: {timestamp} | Timestamp: {ms}
    }
    
    public void logEndTime() {
        // Logs: [REQUEST END] RequestID: {uuid} | Duration: {ms}
    }
}
```

### Integration in LoggingInterceptor

Used via `ObjectFactory<RequestTimingContext>` to create new instance per request:

```java
@Override
public boolean preHandle(HttpServletRequest request, ...) {
    RequestTimingContext timingContext = timingContextFactory.getObject(); // NEW INSTANCE
    timingContext.logStartTime(); // Logs request start
    request.setAttribute("timingContext", timingContext);
}

@Override
public void afterCompletion(HttpServletRequest request, ...) {
    RequestTimingContext timingContext = request.getAttribute("timingContext");
    timingContext.logEndTime(); // Logs request end + duration
}
```

**Log Example:**
```
[REQUEST START] RequestID: a1b2c3d4-e5f6... | Time: 2026-04-05 10:30:45.123 | Timestamp: 1712325045123 ms
[REQUEST END] RequestID: a1b2c3d4-e5f6... | Time: 2026-04-05 10:30:45.456 | Duration: 333 ms
```

**Benefits:**
- ✅ Per-request isolation (no shared state)
- ✅ Accurate request timing measurement
- ✅ Unique request tracing via ID
- ✅ Thread-safe by design

---

## 9. @Lazy Annotation - Deferred Bean Initialization

### What is @Lazy?

@Lazy annotation defers bean creation **until first access** instead of during application startup (eager initialization):

**Eager Initialization (Default):**
```
Application Start → Spring creates ALL singletons → Application Ready
```

**Lazy Initialization (@Lazy):**
```
Application Start → Application Ready → First Access → Bean Created → Used
```

### When to Use @Lazy?

✅ **Beans with expensive initialization** (DB connections, file I/O, external service calls)
✅ **Beans that might not be used** (rarely accessed features, optional functionality)
✅ **Beans with heavy resource requirements** (cache managers, report generators)
✅ **Optional services** (admin features, analytics, reporting)

❌ **NOT for:** Critical startup components, beans that must exist at startup

### ProductReportGenerator - Practical Example

In this project, `ProductReportGenerator` demonstrates @Lazy:

**Without @Lazy (Problem):**
- App startup: +2 seconds (constructor runs immediately)
- Initialization logs print at startup
- Memory used for report cache even if never requested

**With @Lazy (Solution):**
- App startup: immediate (no delay)
- Constructor deferred until report request
- Memory saved if feature never used

### Implementation

Located in: `config/ProductReportGenerator.java`

```java
@Component
@Lazy  // ← Critical: Defers initialization
public class ProductReportGenerator {
    private final ConcurrentHashMap<String, ReportMetadata> reportCache;

    public ProductReportGenerator() {
        logger.info("[LAZY BEAN] ProductReportGenerator - Created on FIRST ACCESS");
        this.reportCache = new ConcurrentHashMap<>();
        
        // Simulate expensive initialization (2 second delay)
        Thread.sleep(2000);
        logger.info("[LAZY BEAN] ProductReportGenerator ready");
    }

    public String generateProductReport(String productId, String productName, double price) {
        String reportId = "REPORT-" + System.currentTimeMillis();
        reportCache.put(reportId, new ReportMetadata(reportId, productId, productName, price, LocalDateTime.now()));
        logger.info("[REPORT] Generated {} for {}", reportId, productName);
        return formatReport(...);
    }
}
```

### Usage in Controller

New endpoint that triggers lazy bean initialization:

```java
@PostMapping("/{id}/report")
public ResponseEntity<String> generateProductReport(@PathVariable Long id) {
    ProductResponse product = productService.getProductById(id);
    
    // Accessing reportGenerator triggers initialization on FIRST CALL ONLY
    String report = reportGenerator.generateProductReport(
        id.toString(), 
        product.getName(), 
        product.getPrice()
    );
    return ResponseEntity.ok(report);
}
```

### Request Timeline Comparison

**First Request: POST /v1/products/1/report**
```
Request arrives
↓
Spring detects reportGenerator needed
↓
@Lazy bean not yet created!
↓
Create instance + run constructor (2 second delay)
↓
[LAZY BEAN] ProductReportGenerator - Created on FIRST ACCESS
[LAZY BEAN] ProductReportGenerator ready  
↓
Execute generateProductReport()
↓
Response: Report data
↓
Total: ~2 seconds (includes initialization)
```

**Subsequent Requests: POST /v1/products/2/report**
```
Request arrives
↓
reportGenerator already initialized ✅ (REUSED singleton)
↓
Execute generateProductReport() immediately
↓
Response: Report data
↓
Total: ~100ms (no initialization overhead!)
```

### How to Inject @Lazy Beans

**Method 1: Direct Injection (Recommended)**
```java
@RestController
public class ProductController {
    private final ProductReportGenerator reportGenerator;
    
    public ProductController(..., ProductReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator; // Created on first use
    }
}
```

**Method 2: ObjectFactory (Explicit Control)**
```java
@RestController
public class ProductController {
    private final ObjectFactory<ProductReportGenerator> generatorFactory;
    
    public ProductController(ObjectFactory<ProductReportGenerator> generatorFactory) {
        this.generatorFactory = generatorFactory;
    }
    
    public void report() {
        // Explicit trigger of lazy initialization
        ProductReportGenerator gen = generatorFactory.getObject();
    }
}
```

### Performance Comparison

| Metric | Eager | @Lazy |
|--------|-------|-------|
| **App Startup** | +2s | ~0s ✅ |
| **First Report Request** | ~100ms | ~2.1s |
| **Subsequent Requests** | ~100ms | ~100ms ✅ |
| **Startup Memory** | +100MB | +0MB ✅ |
| **Use Case** | Critical services | Optional features |

### Best Practices

✅ **Use @Lazy for:**
- Expensive initialization (DB, external API, large cache)
- Optional/rarely-used features
- Heavy resource requirements

❌ **Avoid @Lazy for:**
- Critical startup components
- High-frequency features (user sees 2s delay)
- Core business logic

### Pre-warming Lazy Beans (Optional)

If you want lazy beans initialized in background after startup:

```java
@Component
public class LazyBeanWarmer {
    @Autowired
    private ObjectFactory<ProductReportGenerator> reportGenFactory;
    
    @PostConstruct
    public void preWarmLazyBeans() {
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Wait 3s after app startup
                logger.info("Pre-warming lazy beans...");
                reportGenFactory.getObject(); // Trigger initialization
                logger.info("Lazy beans ready");
            } catch (Exception e) {
                logger.error("Pre-warming failed", e);
            }
        }, "LazyBeanWarmer").start();
    }
}
```

### Testing Lazy Beans

```java
@SpringBootTest
public class LazyBeanTest {
    
    @Test
    public void lazyBeanNotCreatedAtStartup() {
        BeanDefinition beanDef = applicationContext.getBeanFactory()
            .getBeanDefinition("productReportGenerator");
        assertTrue(beanDef.isLazyInit()); // Must be lazy
    }
    
    @Test
    public void lazyBeanCreatedOnFirstAccess() {
        // Verify not created yet
        assertThrows(IllegalStateException.class, 
            () -> applicationContext.getBean(ProductReportGenerator.class, false));
        
        // Trigger creation
        ProductReportGenerator gen1 = applicationContext.getBean(ProductReportGenerator.class);
        assertNotNull(gen1);
        
        // Subsequent access returns same (singleton)
        ProductReportGenerator gen2 = applicationContext.getBean(ProductReportGenerator.class);
        assertSame(gen1, gen2);
    }
}
```

---

## 10. Bean Scoping - Prototype for Multi-Threading

### Use Case: When to Use Prototype Scope?

Prototype scope is beneficial for **stateful business logic** that runs in **multi-threaded environments**:

### Scenario Example

**Problem:** Singleton service with mutable state in multi-threaded context
```java
@Service // Singleton by default
public class ReportService {
    private StringBuilder reportData; // ❌ SHARED across all threads!
    private int progressCount;       // ❌ Race conditions!
    
    public void generateReport() {
        reportData = new StringBuilder();
        progressCount = 0;
        // Multiple threads accessing this = DATA CORRUPTION
    }
}
```

**Solution:** Prototype Scope with Context Sharing
```java
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportContext {
    private final StringBuilder reportData = new StringBuilder();
    private int progressCount = 0;
    private final String contextId = UUID.randomUUID().toString();
    
    public void addData(String data) {
        reportData.append(data);
        progressCount++;
    }
}

@Service
public class ReportService {
    private final ObjectFactory<ReportContext> contextFactory;
    
    public void generateReport() {
        ReportContext context = contextFactory.getObject(); // New instance!
        // Each caller gets isolated context - no thread conflicts
        context.addData("Report data");
    }
}
```

### Key Points

| Aspect | Singleton | Prototype |
|--------|-----------|-----------|
| **Instances** | 1 for entire app | New per injection |
| **Mutable State** | ❌ Unsafe in multi-threaded | ✅ Safe (isolated) |
| **Memory** | Low | Higher |
| **Use Case** | Stateless services | Stateful business logic |
| **Thread Safety** | Requires synchronization | Built-in isolation |

### When Sharing Context is Needed

If multiple threads need to share prototype-scoped context:

```java
// Store in request-scoped holder or pass explicitly
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class ContextHolder {
    private final ReportContext reportContext;
    
    public ContextHolder(ObjectFactory<ReportContext> contextFactory) {
        this.reportContext = contextFactory.getObject();
    }
    
    public ReportContext getContext() {
        return reportContext; // Same context for request lifetime
    }
}

// Multi-threaded task accessing shared context
@Service
public class ReportService {
    public void processInParallel(ReportContext context) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        IntStream.range(0, 10).forEach(i -> 
            executor.submit(() -> context.addData("Item " + i))
        );
    }
}
```

### Best Practices

✅ Use **Prototype** for stateful business logic  
✅ Use **Singleton** for stateless services (ProductService)  
✅ Use **Request** scope for web-specific objects  
✅ When sharing prototype context across threads, use synchronized collections  
✅ Consider using `ConcurrentHashMap` or `Collections.synchronizedList()`  

**In This Project:**
- ✅ ProductService = Singleton (stateless)
- ✅ RequestTimingContext = Prototype (request timing & isolation)

---

## 11. Spring Bean Lifecycle

### What is Bean Lifecycle?

Spring Bean Lifecycle refers to the complete journey of a bean from creation to destruction:

```
Instantiation → Dependency Injection → Initialization → USE → Cleanup → Destruction
```

### Bean Lifecycle Methods (In Order)

Spring provides **multiple hooks** to intervene at each stage:

#### **1️⃣ Constructor (Instantiation)**
```java
public class MyBean {
    public MyBean() {
        // Runs first: Bean instance created
        // Dependencies NOT injected yet
    }
}
```

#### **2️⃣ @PostConstruct (After Dependency Injection)**
```java
@Component
public class MyBean {
    @PostConstruct
    public void postConstruct() {
        // Runs after:
        // - Object instantiated
        // - All dependencies injected ✅
        // - Properties set
        // Best for: initialization logic
    }
}
```

#### **3️⃣ InitializingBean.afterPropertiesSet()**
```java
@Component
public class MyBean implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        // Runs after @PostConstruct
        // Alternative to @PostConstruct (pick one)
    }
}
```

#### **4️⃣ Custom init-method**
```java
@Configuration
public class Config {
    @Bean(initMethod = "customInit")
    public MyBean myBean() {
        return new MyBean();
    }
}

public class MyBean {
    public void customInit() {
        // Runs last in initialization sequence
        // Specified via @Bean(initMethod=...)
    }
}
```

#### **5️⃣ Bean in Use** ✅
```java
// Your application uses the bean
myBean.doSomething();
```

#### **6️⃣ @PreDestroy (Before Shutdown)**
```java
@Component
public class MyBean {
    @PreDestroy
    public void preDestroy() {
        // Runs when application context shuts down
        // Close connections, release resources
    }
}
```

#### **7️⃣ DisposableBean.destroy()**
```java
@Component
public class MyBean implements DisposableBean {
    @Override
    public void destroy() throws Exception {
        // Runs after @PreDestroy
        // Final cleanup
    }
}
```

#### **8️⃣ Custom destroy-method**
```java
@Configuration
public class Config {
    @Bean(destroyMethod = "customDestroy")
    public MyBean myBean() {
        return new MyBean();
    }
}

public class MyBean {
    public void customDestroy() {
        // Runs last in destruction sequence
    }
}
```

### Complete Lifecycle Example

Located in: `config/SpringBeanLifecycleManager.java` *(Single consolidated file)*

This file demonstrates:
- ✅ All 8 lifecycle methods
- ✅ BeanFactoryPostProcessor
- ✅ ApplicationContext & BeanFactory demo methods

```java
@Component
public class SpringBeanLifecycleManager implements InitializingBean, DisposableBean, BeanFactoryPostProcessor {
    
    // 1. Constructor
    public SpringBeanLifecycleManager() { }
    
    // 2. @PostConstruct
    @PostConstruct
    public void postConstruct() { }
    
    // 3. InitializingBean
    @Override
    public void afterPropertiesSet() { }
    
    // 4. Custom init-method
    public void customInit() { }
    
    // 5. Bean in use
    public void doWork() { }
    
    // 6. @PreDestroy
    @PreDestroy
    public void preDestroy() { }
    
    // 7. DisposableBean
    @Override
    public void destroy() { }
    
    // 8. Custom destroy-method
    public void customDestroy() { }
    
    // BeanFactoryPostProcessor
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) { }
    
    // Demo methods
    public void demonstrateApplicationContext(ApplicationContext context) { }
    public void demonstrateBeanFactory(BeanFactory factory) { }
}
```

**Log Output:**
```
[1] Constructor - Instantiation
[2] @PostConstruct - Dependencies injected
[3] InitializingBean.afterPropertiesSet()
[4] Custom init-method
✅ BEAN READY FOR USE
[5] Bean in use - doing work
(... application running ...)
[6] @PreDestroy - Shutdown initiated
[7] DisposableBean.destroy()
[8] Custom destroy-method
✅ BEAN DESTROYED
```

### Best Practices for Lifecycle Methods

✅ **Use @PostConstruct** over InitializingBean (more modern, less coupled)
```java
@PostConstruct
public void initialize() { }  // ✅ Preferred
```

❌ **Avoid overusing lifecycle methods** - Keep them simple
```java
@PostConstruct
public void init() {
    // Only essential initialization
    // Don't do complex business logic here
}
```

✅ **Handle exceptions properly**
```java
@PostConstruct
public void init() {
    try {
        connectToDatabase();
    } catch (Exception e) {
        throw new BeanInitializationException("Failed to init", e);
    }
}
```

✅ **Use @PreDestroy for cleanup**
```java
@PreDestroy
public void cleanup() {
    closeConnections();
    flushCaches();
    releaseResources();
}
```

---

## 12. BeanFactory and ApplicationContext

### What is BeanFactory?

**BeanFactory** is the root interface for accessing Spring beans:

```java
// Get bean by name
Object bean = beanFactory.getBean("myBean");

// Get bean by type
MyBean bean = beanFactory.getBean(MyBean.class);

// Check if bean is singleton or prototype
boolean isSingleton = beanFactory.isSingleton("myBean");
```

### BeanFactory Key Methods

| Method | Purpose | Example |
|--------|---------|---------|
| `getBean(String name)` | Get bean by name | `beanFactory.getBean("productService")` |
| `getBean(Class type)` | Get bean by type | `beanFactory.getBean(ProductService.class)` |
| `containsBean(String)` | Check bean exists | `beanFactory.containsBean("productService")` |
| `isSingleton(String)` | Check if singleton | `beanFactory.isSingleton("productService")` |
| `isPrototype(String)` | Check if prototype | `beanFactory.isPrototype("beanLifecycleDemo")` |
| `getType(String)` | Get bean class type | `beanFactory.getType("productService")` |

### BeanFactory Characteristics

✅ **Lazy Initialization** - Beans created only when requested
✅ **Lightweight** - Minimal overhead, suitable for embedded apps
✅ **Basic Features** - Bean creation, dependency injection
❌ **No AOP** - Aspect-Oriented Programming not supported
❌ **No Event Publishing** - Can't publish events

### BeanFactory Usage Example

```java
@Component
public class BeanFactoryDemo {
    
    private final BeanFactory beanFactory;
    
    public BeanFactoryDemo(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    public void demonstrateBeanFactory() {
        // Get bean by name
        ProductService service = (ProductService) 
            beanFactory.getBean("productService");
        
        // Get bean by type
        ProductService service2 = 
            beanFactory.getBean(ProductService.class);
        
        // Check scope
        if (beanFactory.isSingleton("productService")) {
            logger.info("productService is a Singleton");
        }
        
        // Get bean type
        Class<?> type = beanFactory.getType("productService");
        logger.info("Bean type: {}", type.getSimpleName());
    }
}
```

### ApplicationContext - Advanced Bean Management

### What is ApplicationContext?

**ApplicationContext** extends BeanFactory with enterprise features:

```
BeanFactory (Root Interface)
    ↓
ApplicationContext (Advanced Interface)
    ├─ AOP support
    ├─ Event publishing
    ├─ Message source (i18n)
    ├─ Resource loading
    ├─ Parent context support
    └─ Lifecycle management
```

### ApplicationContext vs BeanFactory

| Feature | BeanFactory | ApplicationContext |
|---------|-------------|-------------------|
| **Bean Instantiation** | Lazy (on demand) | Eager (at startup) |
| **AOP Support** | ❌ No | ✅ Yes |
| **Event Publishing** | ❌ No | ✅ Yes |
| **Message Source** | ❌ No | ✅ Yes |
| **Resource Loading** | ❌ No | ✅ Yes |
| **Bean Post-Processors** | ✅ Yes | ✅ Yes |
| **Use Case** | Embedded/Lightweight | Enterprise apps |
| **Performance** | Faster startup | Slower startup |

### ApplicationContext Key Methods

```java
// Get bean by name
Object bean = applicationContext.getBean("myBean");

// Get bean by type
MyBean bean = applicationContext.getBean(MyBean.class);

// Check bean existence
boolean exists = applicationContext.containsBean("myBean");

// Get all bean names
String[] names = applicationContext.getBeanDefinitionNames();

// Get total bean count
int count = applicationContext.getBeanDefinitionCount();

// Access environment
String activeProfile = applicationContext.getEnvironment().getActiveProfiles()[0];

// Publish events
applicationContext.publishEvent(new MyEvent());
```

### ApplicationContext Usage Example

Located in: `config/SpringBeanLifecycleManager.java` *(Consolidated with all lifecycle methods)*

```java
@Component
public class SpringBeanLifecycleManager {
    
    private final ApplicationContext applicationContext;
    
    public SpringBeanLifecycleManager(ApplicationContext context) {
        this.applicationContext = context;
    }
    
    public void demonstrateApplicationContext() {
        logger.info("[CONTEXT] ApplicationContext Demonstration");
        
        // Get bean by name
        ProductService service = 
            applicationContext.getBean("productService", ProductService.class);
        logger.info("      ✅ Retrieved ProductService bean");
        
        // Get all bean names
        String[] beans = applicationContext.getBeanDefinitionNames();
        logger.info("      Total beans in context: {}", beans.length);
        
        // Get active profile
        String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
        logger.info("      Active profile: {}", String.join(",", profiles));
        
        // Publish event
        applicationContext.publishEvent(new MyStartupEvent());
        logger.info("      ✅ Published startup event");
    }
}
```

---

## 13. BeanFactoryPostProcessor

### What is BeanFactoryPostProcessor?

**BeanFactoryPostProcessor** intercepts and modifies bean definitions **BEFORE** beans are instantiated:

```
Spring loads bean definitions
    ↓
BeanFactoryPostProcessor.postProcessBeanFactory() ← YOU ARE HERE
    ↓
Beans are instantiated
    ↓
Beans are initialized
```

### Execution Timeline

```
Application Startup
    ↓
1. Bean definitions loaded from classpath
    ↓
2. BeanFactoryPostProcessor.postProcessBeanFactory() runs
    │  ├─ Modify bean scopes
    │  ├─ Register new beans
    │  ├─ Change property values
    │  └─ Add bean post-processors
    ↓
3. Beans instantiated (constructors called)
    ↓
4. Dependencies injected
    ↓
5. @PostConstruct and init-methods called
    ↓
6. Application ready ✅
```

### BeanFactoryPostProcessor Interface

```java
public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
        throws BeansException;
}
```

**Key Point:** Runs **BEFORE any bean is created** ⏰

### Complete Example

Located in: `config/SpringBeanLifecycleManager.java` *(Consolidated with all lifecycle methods)*

```java
@Component
public class SpringBeanLifecycleManager implements BeanFactoryPostProcessor {
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) 
            throws BeansException {
        
        logger.info("[BOOT] BeanFactoryPostProcessor running (BEFORE bean instantiation)");
        
        // Get all bean definition names
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        logger.info("      Total beans defined: {}", beanNames.length);
        
        // Modify specific bean definition
        if (beanFactory.containsBeanDefinition("productService")) {
            BeanDefinition beanDef = beanFactory.getBeanDefinition("productService");
            
            // Example modifications:
            beanDef.setScope(BeanDefinition.SCOPE_PROTOTYPE); // Change scope
            beanDef.setLazyInit(true); // Enable lazy init
            
            logger.info("      ↳ Modified productService bean definition");
        }
        
        // Register a new bean programmatically
        // beanFactory.registerSingleton("newBean", new MyBean());
    }
}
```

### Use Cases for BeanFactoryPostProcessor

✅ **Dynamic Bean Registration**
```java
// Register beans based on conditions
if (isProDEnv()) {
    beanFactory.registerSingleton("datasource", 
        new ProductionDataSource());
}
```

✅ **Modify Bean Scopes**
```java
// Change all service beans to prototype
for (String name : beanFactory.getBeanDefinitionNames()) {
    if (name.endsWith("Service")) {
        BeanDefinition def = beanFactory.getBeanDefinition(name);
        def.setScope(BeanDefinition.SCOPE_PROTOTYPE);
    }
}
```

✅ **Conditional Bean Creation**
```java
// Only register feature-specific beans if feature enabled
if (featureToggle.isEnabled("reporting")) {
    beanFactory.registerSingleton("reportingService", 
        new ReportingService());
}
```

✅ **Property Value Injection**
```java
// Override property values in bean definitions
for (String name : beanFactory.getBeanDefinitionNames()) {
    BeanDefinition def = beanFactory.getBeanDefinition(name);
    MutablePropertyValues props = def.getPropertyValues();
    props.add("timeout", "300000");
}
```

### BeanFactoryPostProcessor vs BeanPostProcessor

| Aspect | BeanFactoryPostProcessor | BeanPostProcessor |
|--------|--------------------------|-------------------|
| **When Runs** | Before beans created | After beans created |
| **Access** | Bean definitions | Bean instances |
| **Can do** | Modify definitions, register beans | Wrap beans, modify behavior |
| **Method** | `postProcessBeanFactory()` | `postProcessBefore/AfterInitialization()` |
| **Use for** | Conditional bean registration | AOP, proxies, decorators |

### Best Practices

✅ **Use @Component** to auto-register BeanFactoryPostProcessor
```java
@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    // Auto-detected and executed
}
```

✅ **Keep logic focused** - Don't do complex operations
```java
public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    // Only modify bean definitions
    // Don't do heavy I/O or network calls
}
```

✅ **Log for debugging** - Help understand what's modified
```java
logger.info("BeanFactoryPostProcessor: Modified {} bean definitions", count);
```

❌ **Don't instantiate beans** - Defeats the purpose
```java
// ❌ WRONG - Don't do this
MyBean bean = beanFactory.getBean("myBean");

// ✅ CORRECT - Only modify definitions
BeanDefinition def = beanFactory.getBeanDefinition("myBean");
def.setLazyInit(true);
```

---

## 14. Validation and Error Handling

### GlobalExceptionHandler
Located in: `Exceptions/GlobalExceptionHandler.java`

Uses `@RestControllerAdvice` for centralized, REST-specific exception handling:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setStatus(404);
        errorResponse.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(404).body(errorResponse);
    }
}
```

**Handles:**
- ✅ ProductNotFoundException (404 Not Found)
- ✅ Validation errors (400 Bad Request)
- ✅ Generic exceptions (500 Internal Server Error)

**Example Error Response:**
```json
{
  "status": 404,
  "message": "Product not found with id: 1",
  "timestamp": 1712325045000
}
```

---

### @RestControllerAdvice vs @ControllerAdvice

| Aspect | @ControllerAdvice | @RestControllerAdvice |
|--------|------------------|----------------------|
| **Purpose** | Global exception handling for controllers | Exception handling + automatic JSON serialization |
| **Response Type** | ModelAndView, String, HTML (View-based) | JSON (REST responses) |
| **@ResponseBody Required** | ❌ Must add to methods | ✅ Built-in |
| **Use Case** | MVC applications (HTML views) | REST APIs, Microservices |
| **Serialization** | Manual (ViewResolver) | Automatic (HttpMessageConverter) |
| **Best For** | Traditional Spring MVC | REST/JSON endpoints |
| **Syntax** | `@ControllerAdvice` | `@RestControllerAdvice` |

### When to Use Each

**@ControllerAdvice** - Traditional MVC with views:
```java
@ControllerAdvice
public class GlobalMvcExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    // Returns ModelAndView for HTML rendering
    public ModelAndView handleException(Exception ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", ex.getMessage());
        return mav; // ← View-based response
    }
}
```

**@RestControllerAdvice** - REST APIs with JSON:
```java
@RestControllerAdvice  // ← Recommended for REST APIs
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    // Automatically converts to JSON
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse response = new ErrorResponse();
        response.setMessage(ex.getMessage());
        response.setStatus(500);
        return ResponseEntity.status(500).body(response); // ← Auto JSON serialization
    }
}
```

### @RestControllerAdvice Breakdown

**Equivalent to:**
```java
@ControllerAdvice
@ResponseBody  // ← Added automatically
public class GlobalExceptionHandler {
    // All methods return JSON automatically
}
```

### Exception Hierarchy in This Project

```
Throwable
│
└── Exception
    ├── ProductNotFoundException (Custom - handled → 404)
    └── Other Exceptions (Handled → 500)
```

### Handler Method Signature

```java
@ExceptionHandler(ProductNotFoundException.class)
public ResponseEntity<ErrorResponse> handleProductNotFoundException(
    ProductNotFoundException ex  // ← Exception parameter
) {
    // Exception details available in 'ex'
    logger.error("Product not found: {}", ex.getMessage());
    
    ErrorResponse response = new ErrorResponse();
    response.setMessage(ex.getMessage());
    response.setStatus(404);
    
    // Automatically serialized to JSON by @RestControllerAdvice
    return ResponseEntity.status(404).body(response);
}
```

### Best Practices for Exception Handling

✅ **Use @RestControllerAdvice** for REST APIs  
✅ **Log all exceptions** with proper context  
✅ **Return consistent error format** (ErrorResponse)  
✅ **Include HTTP status codes** (404, 400, 500)  
✅ **Add timestamps** for debugging  
✅ **Don't expose internal details** in production  
✅ **Handle specific exceptions first**, then generic  
✅ **Use proper HTTP status codes** (not always 500)  

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Specific exceptions first
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle404(ProductNotFoundException ex) {
        logger.warn("Product not found: {}", ex.getMessage());
        return buildErrorResponse("Not Found", 404, ex.getMessage());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handle400(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        return buildErrorResponse("Bad Request", 400, ex.getMessage());
    }
    
    // Generic exception last
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle500(Exception ex) {
        logger.error("Unexpected error: ", ex);
        return buildErrorResponse("Internal Server Error", 500, "An error occurred");
    }
    
    private ResponseEntity<ErrorResponse> buildErrorResponse(String title, int status, String message) {
        ErrorResponse response = new ErrorResponse();
        response.setMessage(message);
        response.setStatus(status);
        response.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(status).body(response);
    }
}
```

### Error Response Flow

```
Client Request
      ↓
Controller Method
      ↓
Exception Thrown
      ↓
@RestControllerAdvice catches it
      ↓
@ExceptionHandler processes it
      ↓
Returns ResponseEntity<ErrorResponse>
      ↓
Automatically serialized to JSON
      ↓
Client receives:
{
  "status": 404,
  "message": "Product not found",
  "timestamp": 1712325045000
}
```

### Advantages of @RestControllerAdvice

✅ **Centralized:** One place for all exception handling  
✅ **Consistent:** Same error format for all endpoints  
✅ **DRY:** No exception handling code in controllers  
✅ **Maintainable:** Easy to add/modify handlers  
✅ **REST-friendly:** Automatic JSON serialization  
✅ **Logging:** Log exceptions in one place  
✅ **Clean URLs:** Error details in response body  

---

## 15. Logging and MDC

### Logback Configuration
Located in: `resources/logback-spring.xml`

**Console Output Pattern:**
```
2026-04-05 10:30:45 - Creating product Laptop
```

**Log Levels by Profile:**
- **dev:** DEBUG (verbose)
- **prod:** INFO (important events only)
- **test:** WARN (only warnings and errors)

### Logs from Application
```logs/
├── application.log (all logs)
└── error.log (errors only)
```

---

## 16. MDC Propagation Across Threads

### Problem: MDC is Thread-Local

MDC stores context information **per thread**. When you create child threads (async tasks, thread pools), they don't inherit parent thread's MDC:

```java
@GetMapping("/products")
public ResponseEntity<List<ProductResponse>> getAllProducts() {
    MDC.put("correlationId", "req-123");
    
    // Child thread ❌ NO correlationId here!
    new Thread(() -> {
        String id = MDC.get("correlationId"); // Returns null!
        log.info("Child thread: {}", id);     // No context!
    }).start();
    
    return ResponseEntity.ok(productService.getAllProducts(null));
}
```

### Solution 1: Manual MDC Copying (Simple Tasks)

Copy MDC before spawning thread and restore in child:

```java
@Service
public class AsyncProductService {
    
    public void processProductsAsync() {
        // Get current MDC context
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        
        new Thread(() -> {
            // Restore MDC in child thread
            if (mdcContext != null) {
                mdcContext.forEach(MDC::put);
            }
            
            try {
                String correlationId = MDC.get("correlationId");
                log.info("Child thread with context: {}", correlationId);
                // Do work
            } finally {
                MDC.clear(); // Clean up
            }
        }).start();
    }
}
```

### Solution 2: ExecutorService with Custom ThreadFactory (Recommended)

Create custom `ThreadFactory` that propagates MDC:

```java
@Configuration
public class AsyncConfig {
    
    @Bean(name = "mdcAwareExecutor")
    public ExecutorService mdcAwareExecutor() {
        return Executors.newFixedThreadPool(4, new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(0);
            
            @Override
            public Thread newThread(Runnable r) {
                Map<String, String> parentMdc = MDC.getCopyOfContextMap();
                
                return new Thread(() -> {
                    // Restore parent MDC
                    if (parentMdc != null) {
                        parentMdc.forEach(MDC::put);
                    }
                    
                    try {
                        r.run();
                    } finally {
                        MDC.clear();
                    }
                }, "MDC-Thread-" + count.incrementAndGet());
            }
        });
    }
}
```

**Usage:**
```java
@Service
public class ProductService {
    
    @Autowired
    @Qualifier("mdcAwareExecutor")
    private ExecutorService executorService;
    
    public void processInParallel(List<Product> products) {
        products.forEach(product -> 
            executorService.submit(() -> {
                String correlationId = MDC.get("correlationId"); // ✅ Available!
                log.info("Processing product {} with correlation: {}", 
                    product.getId(), correlationId);
                // Process product
            })
        );
    }
}
```

### Solution 3: TaskDecorator (Spring Boot Best Practice)

Use Spring's `TaskDecorator` for clean MDC propagation:

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        
        // Critical: Propagate MDC to async threads
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }
}

// Custom TaskDecorator for MDC propagation
public class MdcTaskDecorator implements TaskDecorator {
    
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        
        return () -> {
            if (mdcContext != null) {
                mdcContext.forEach(MDC::put);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
```

**Usage with Spring @Async:**
```java
@Service
public class ProductService {
    
    @Async
    public void asyncProcessProduct(Product product) {
        String correlationId = MDC.get("correlationId"); // ✅ Available!
        log.info("Async processing product {} | Correlation: {}", 
            product.getId(), correlationId);
        // Do async work
    }
}

// Call from controller
@GetMapping("/{id}/process-async")
public ResponseEntity<String> processAsync(@PathVariable Long id) {
    productService.asyncProcessProduct(product);
    return ResponseEntity.ok("Processing started");
}
```

### Solution 4: Using CompletableFuture with Custom Supplier

For modern async patterns:

```java
@Service
public class ProductService {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    public CompletableFuture<ProductResponse> processProductAsync(Long id) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        
        return CompletableFuture.supplyAsync(() -> {
            // Restore MDC
            if (mdcContext != null) {
                mdcContext.forEach(MDC::put);
            }
            
            try {
                String correlationId = MDC.get("correlationId");
                log.info("Processing product {} | Correlation: {}", id, correlationId);
                
                Product product = getProductById(id);
                return mapToResponse(product);
            } finally {
                MDC.clear();
            }
        }, executor);
    }
}
```

### MDC Context Using RequestTimingContext

In this project, `RequestTimingContext` captures MDC information:

```java
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RequestTimingContext {
    private final String requestId = UUID.randomUUID().toString();
    
    public void logStartTime() {
        // Current MDC available here (same thread as request)
        String correlationId = MDC.get("correlationId");
        logger.info("[REQUEST START] RequestID: {} | CorrelationID: {} | Time: {}", 
            requestId, correlationId, startTime.format(formatter));
    }
}
```

**For async operations:**
```java
@Service
public class ProductService {
    
    @Autowired
    @Qualifier("mdcAwareExecutor")
    private ExecutorService executor;
    
    public CompletableFuture<List<ProductResponse>> getAllProductsAsync() {
        return CompletableFuture.supplyAsync(
            () -> productRepository.findAll(),
            executor // ✅ MDC automatically propagated
        );
    }
}
```

### Comparison of Approaches

| Approach | Use Case | Complexity | Best For |
|----------|----------|-----------|----------|
| **Manual Copy** | Simple one-off tasks | ⭐⭐ | Quick async operations |
| **Custom ThreadFactory** | Multiple thread pools | ⭐⭐⭐ | Complex threading logic |
| **TaskDecorator** | Spring @Async | ⭐⭐ | **Recommended** |
| **CompletableFuture** | Modern async flow | ⭐⭐⭐ | Non-blocking chains |

### Best Practices for MDC in Multi-Threading

✅ **Always copy MDC before creating threads**
```java
Map<String, String> mdcContext = MDC.getCopyOfContextMap();
```

✅ **Restore in child thread**
```java
if (mdcContext != null) {
    mdcContext.forEach(MDC::put);
}
```

✅ **Clean up in finally block**
```java
try {
    // work
} finally {
    MDC.clear();
}
```

✅ **Use Spring @EnableAsync with TaskDecorator** (Recommended)

✅ **Don't share MDC across unrelated requests**

✅ **Keep correlationId and requestId consistent**

✅ **Test MDC propagation in integration tests**

### Testing MDC Propagation

```java
@Test
public void testMdcPropagationInAsyncTask() throws Exception {
    MDC.put("correlationId", "test-123");
    
    Map<String, String> mdcContext = MDC.getCopyOfContextMap();
    CountDownLatch latch = new CountDownLatch(1);
    
    new Thread(() -> {
        if (mdcContext != null) {
            mdcContext.forEach(MDC::put);
        }
        
        try {
            String correlationId = MDC.get("correlationId");
            assertEquals("test-123", correlationId); // ✅ Passes
        } finally {
            MDC.clear();
            latch.countDown();
        }
    }).start();
    
    latch.await();
}
```

---

## 17. Swagger/OpenAPI Documentation

### Unified Swagger Configuration

Located in: `config/SwaggerConfig.java` *(Merged from 2 files)*

**Single configuration file with:**
- ✅ OpenAPI schema definition
- ✅ API info (title, version, description)
- ✅ Global header parameters (correlationId, employeeId)
- ✅ OperationCustomizer for automatic header injection

```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        // Define API info and reusable parameters
    }
    
    @Bean
    public OperationCustomizer customHeaders() {
        // Add X-Correlation-Id and X-Employee-Id to ALL endpoints
    }
}
```

**Note:** Previous separate configs (SwaggerConfig + SwaggerGlobalHeaderConfig) have been consolidated into one file for better maintainability.

### Access Swagger UI
- **URL:** `http://localhost:8081/swagger-ui.html`
- **Features:** 
  - Interactive API testing
  - Request/response examples
  - Global parameters visible on all endpoints (correlationId, employeeId)

### OpenAPI JSON
- **URL:** `http://localhost:8081/v3/api-docs`
- **Use:** For programmatic API consumption, API gateway setup

### Global Header Parameters (Auto-Injected)

These headers are **automatically added to all endpoints** via `OperationCustomizer`:

| Header | Type | Required | Purpose |
|--------|------|----------|---------|
| **X-Correlation-Id** | String | No | Distributed request tracing, MDC propagation |
| **X-Employee-Id** | String | No | User identification, audit logging |

**Example Request:**
```bash
curl -X GET http://localhost:8081/v1/products \
  -H "X-Correlation-Id: req-12345" \
  -H "X-Employee-Id: emp-789"
```

**All endpoints automatically include these in Swagger documentation** without additional @Parameter annotations!

---

## 18. @Conditional + @Profile + @Qualifier

```java
// @Profile: different beans per environment
@Bean @Profile("dev")
public NotificationService consoleNotification() { ... }

@Bean @Profile("prod")
public NotificationService emailNotification() { ... }

// @ConditionalOnProperty: bean created only if property is true
@Bean
@ConditionalOnProperty(name = "product.service.cache-enabled", havingValue = "true")
public String cacheEnabledMarker() { ... }
```

---

## 19. Spring Boot DevTools

### What is DevTools?

Spring Boot DevTools provides **live reload and automatic restart** during development:

✅ **Automatic Restart** - Code changes trigger app restart (seconds, not minutes)
✅ **Live Reload** - Browser automatically refreshes on static file changes
✅ **Faster Development** - No need to manually stop/start the application
✅ **Debug-Friendly** - Maintains debug session across restarts
✅ **Configuration** - Hot reload for property files (application.properties)

### DevTools Already Included

DevTools is now added to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

It automatically activates during development (not in production JAR).

### Using DevTools in Development

**Run with Maven:** (DevTools activates automatically)
```bash
mvn spring-boot:run
```

**Or Run JAR:** (DevTools only works during development, not in packaged JAR)
```bash
java -jar target/product-*.jar
```

### What Gets Auto-Restarted?

| File Change | Auto-Restart | Notes |
|-------------|--------------|-------|
| **Java source code** | ✅ Yes | Controller, Service, Config changes |
| **application.properties** | ✅ Yes | Profile configs, app properties |
| **application-{profile}.properties** | ✅ Yes | Dev, prod, test profile changes |
| **Static files** (CSS, JS, HTML) | ⚡ Live reload | Browser refresh only (no restart) |
| **Template files** (Thymeleaf, Freemarker) | ⚡ Live reload | Template changes detected |

### Development Workflow with DevTools

```
1. Start app with: mvn spring-boot:run
2. Make code changes (e.g., ProductController.java)
3. Save file
4. DevTools detects file change
5. Automatic restart (5-10 seconds)
6. Test changes in browser/Postman
7. Repeat steps 2-6
```

**Timeline:**
```
Without DevTools:
- Modify code → Kill app → mvn clean package → mvn spring-boot:run → ~30 seconds ❌

With DevTools:
- Modify code → Save → Auto restart → ~5 seconds ✅
```

### DevTools Features

#### 1. **Property Changes Auto-Reload**
Change `application-dev.properties`:
```properties
app.environment=development
app.api.timeout=5000  ← Change this
```
Save → Auto-restart → New config value applied ✅

#### 2. **Code Changes Auto-Restart**
Modify controller:
```java
@PostMapping
public ResponseEntity<ProductResponse> createProduct(...) {
    logger.info("Creating product"); // Add this line
    // ...
}
```
Save → Auto-restart → Method updated ✅

#### 3. **Static File Live Reload** (Browser only)
Edit stylesheet:
```css
/* styles.css */
body { color: blue; } /* Change this */
```
Save → Browser auto-refreshes → Sees new color ✅

### Disabling DevTools (When Not Needed)

If you want to disable DevTools:

**Option 1: Remove from classpath**
```bash
mvn spring-boot:run -Dspring-boot.devtools.restart.enabled=false
```

**Option 2: In application.properties**
```properties
spring.devtools.restart.enabled=false
```

**Option 3: Exclude from JAR (automatic)**
DevTools only works during development, not in production JAR builds.

### IDE Configuration for DevTools

#### **IntelliJ IDEA**
Enable compiler features for faster restart:
- **Settings → Build, Execution, Deployment → Compiler → Build project automatically** ✅
- **File → Settings → Advanced Settings → Allow auto-make to start even if disabled**

Then DevTools works seamlessly:
```
Save file → IntelliJ compiles → DevTools detects → App restarts
```

#### **Eclipse**
Enable auto-build:
- **Project → Build Automatically** ✅
- **Eclipse → Preferences → General → Workspace → Build automatically**

#### **VS Code + Maven**
Use Extension Packs:
- Spring Boot Extension Pack
- Maven for Java

Then:
```bash
mvn spring-boot:run -Dspring-jvm.args=-Dspring.devtools.restart.enabled=true
```

### Restart Exclusions

Some files don't need restart (modify `application.properties`):

```properties
# Exclude patterns from restart
spring.devtools.restart.exclude=static/**,public/**
```

This prevents restarting on static file changes (uses live reload instead).

### DevTools ClassLoader Magic

DevTools uses two classloaders:

```
┌─────────────────────────────────────┐
│   Base ClassLoader (External JARs)  │ ← Restored across restarts
│   (Spring, Lombok, Jackson, etc.)   │
└─────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────┐
│   Restart ClassLoader (Your Code)   │ ← Recreated on each restart
│   (Controllers, Services, etc.)     │   (Fast reload, seconds)
└─────────────────────────────────────┘
```

**Result:** Only your code reloads (fast), not external libraries (slow).

### DevTools Performance Tips

✅ **Exclude unchanging files:**
```properties
spring.devtools.restart.exclude=logs/**,temp/**
```

✅ **Increase restart timeout if app is slow:**
```properties
spring.devtools.restart.poll-interval=2000
spring.devtools.restart.quiet-period=1000
```

✅ **Use profile for dev only:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### DevTools with This Project

In this project, DevTools helps during development:

1. **Change ApplicationProperties.java** → Auto-restart
2. **Modify ProductController endpoints** → Auto-restart
3. **Update application-dev.properties** → Auto-restart with new config
4. **Edit ProductService business logic** → Auto-restart
5. **Tweak ProductReportGenerator** → Auto-restart (@Lazy bean reinitializes)

**Example Workflow:**
```bash
# 1. Start development server
mvn spring-boot:run

# 2. App runs, test in another terminal
curl http://localhost:8081/v1/products

# 3. Modify ProductService.java (add logging, fix bug, etc.)

# 4. Save file → DevTools auto-restarts (5 seconds)
[INFO] Restarting application
[INFO] Application started

# 5. Re-run curl command → See new changes ✅
curl http://localhost:8081/v1/products
```

---

## 20. Spring Boot Actuator

### What is Actuator?

Spring Boot **Actuator** provides production-ready endpoints for:
- 🏥 **Health Checks** - Application status monitoring
- 📊 **Metrics** - Performance & runtime metrics
- 🔍 **Diagnostics** - Detailed application information
- 🎯 **Liveness/Readiness Probes** - Kubernetes probe support

### Available Endpoints

#### Health Endpoint
```http
GET /actuator/health HTTP/1.1
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "livenessState": {
      "status": "CORRECT"
    },
    "readinessState": {
      "status": "ACCEPTING_TRAFFIC"
    }
  }
}
```

#### Common Actuator Endpoints

| Endpoint | Purpose | Example |
|----------|---------|---------|
| `/actuator/health` | Application health status | `GET /actuator/health` |
| `/actuator/metrics` | List all available metrics | `GET /actuator/metrics` |
| `/actuator/metrics/{metric}` | Specific metric value | `GET /actuator/metrics/jvm.memory.used` |
| `/actuator/info` | Application information | `GET /actuator/info` |
| `/actuator/env` | Environment properties | `GET /actuator/env` |
| `/actuator/loggers` | Logging configuration | `GET /actuator/loggers` |
| `/actuator/threaddump` | Thread dump | `GET /actuator/threaddump` |
| `/actuator/prometheus` | Prometheus metrics | `GET /actuator/prometheus` |

### Profile-Specific Configuration

#### Development Profile (`dev`)
**All endpoints exposed for full debugging:**
```properties
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

✅ Access all endpoints during development  
✅ View detailed health information  
✅ Full metrics visibility  

**Commands:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev

# View health with details
curl http://localhost:8080/actuator/health

# List all metrics
curl http://localhost:8080/actuator/metrics

# Get specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

#### Production Profile (`prod`)
**Restricted endpoints for security:**
```properties
management.endpoints.web.exposure.include=health,metrics,info,prometheus
management.endpoint.health.show-details=when-authorized
```

✅ Only essential endpoints exposed  
✅ Health details require authorization  
✅ Prometheus metrics for monitoring  

**Available Endpoints:**
- `/actuator/health` - Basic health (restricted to authorized users)
- `/actuator/metrics` - Metrics collection only
- `/actuator/info` - Application info
- `/actuator/prometheus` - Prometheus format metrics

#### Test Profile (`test`)
**Moderate exposure for testing:**
```properties
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=always
```

✅ Essential endpoints enabled  
✅ Detailed health info for testing  
✅ Metrics available

### Liveness & Readiness Probes

Spring Boot Actuator provides probes for Kubernetes orchestration:

#### Liveness State (`/health/liveness`)
Indicates if the application is **running** (not crashed):
```bash
curl http://localhost:8080/actuator/health/liveness
# Response: {"status":"CORRECT"} = Running
```

#### Readiness State (`/health/readiness`)
Indicates if the application is **ready to accept traffic**:
```bash
curl http://localhost:8080/actuator/health/readiness
# Response: {"status":"ACCEPTING_TRAFFIC"} = Ready
```

### Metrics Examples

#### JVM Memory Metrics
```bash
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.memory.max
curl http://localhost:8080/actuator/metrics/jvm.memory.committed
```

#### HTTP Request Metrics
```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

#### System Metrics
```bash
curl http://localhost:8080/actuator/metrics/system.cpu.usage
curl http://localhost:8080/actuator/metrics/process.uptime
```

### Custom Metrics (Optional Enhancement)

You can add custom metrics if needed:

```java
@Component
public class CustomMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Custom gauge
        meterRegistry.gauge("products.total", () -> 100);
        
        // Custom counter
        meterRegistry.counter("api.requests.created").increment();
    }
}
```

### Integration with Monitoring Tools

#### Prometheus Integration
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'product-service'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

#### Kubernetes Probe Configuration
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: product-service
spec:
  containers:
  - name: app
    image: product-service:latest
    livenessProbe:
      httpGet:
        path: /actuator/health/liveness
        port: 8080
      initialDelaySeconds: 10
      periodSeconds: 10
    readinessProbe:
      httpGet:
        path: /actuator/health/readiness
        port: 8080
      initialDelaySeconds: 5
      periodSeconds: 5
```

### Best Practices

1. ✅ **Development**: Expose all endpoints for debugging
2. 🔒 **Production**: Restrict to health, metrics, info only
3. 📊 **Monitoring**: Use Prometheus endpoint for metrics scraping
4. 🎯 **Kubernetes**: Configure liveness & readiness probes
5. 🔐 **Security**: Protect `/actuator/env` and `/actuator/loggers`
6. ⏰ **Alerts**: Set up alerts based on health status

### Actuator in Distributed Systems

> **Note:** Basic Actuator docs are already covered later in this README. This section covers Actuator in the context of our distributed saga setup.

### Actuator in Distributed Systems

When running multiple microservices (Order + Product), Actuator helps monitor each independently:

```bash
# Product Service health
curl http://localhost:8080/actuator/health

# Order Service health
curl http://localhost:8081/actuator/health
```

### Kafka Health Indicator

When Kafka is connected, Actuator automatically adds a Kafka health check:

```json
{
  "status": "UP",
  "components": {
    "kafka": {
      "status": "UP",
      "details": {
        "clusterId": "abc-123"
      }
    }
  }
}
```

If Kafka is down:
```json
{
  "status": "DOWN",
  "components": {
    "kafka": {
      "status": "DOWN",
      "details": {
        "error": "Connection refused"
      }
    }
  }
}
```

### Useful Actuator Endpoints for Debugging Saga

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Check if service + Kafka are UP |
| `/actuator/metrics/kafka.consumer.records-consumed-total` | Total Kafka messages consumed |
| `/actuator/metrics/kafka.producer.record-send-total` | Total Kafka messages sent |
| `/actuator/metrics/http.server.requests` | REST API call metrics |
| `/actuator/loggers/com.example.product` | View/change log level at runtime |

### Changing Log Level at Runtime (Without Restart)

```bash
# Check current log level
curl http://localhost:8080/actuator/loggers/com.example.product.product.service.ProductService

# Change to DEBUG
curl -X POST http://localhost:8080/actuator/loggers/com.example.product.product.service.ProductService \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# Change to WARN (reduce noise in prod)
curl -X POST http://localhost:8080/actuator/loggers/com.example.product.product.service.ProductService \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "WARN"}'
```

### Actuator Configuration in Our Project

```properties
# application-dev.properties (all endpoints exposed)
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.endpoint.health.show-components=always
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true

# application-prod.properties (restricted)
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

---

## 21. Saga Pattern with Kafka

### Problem: No Distributed Transactions

In a monolith, a single database transaction guarantees ACID properties. In microservices, each service has its own data store — **there is no global transaction**. If Order Service creates an order but Product Service fails to reduce stock, data becomes inconsistent.

```
❌ MONOLITH WAY (won't work in microservices):
@Transactional
public void placeOrder() {
    orderRepo.save(order);       // DB 1
    productRepo.reduceStock();   // DB 2 — DIFFERENT SERVICE!
}
```

### Solution: Saga Pattern

A **Saga** is a sequence of local transactions where each service performs its own transaction and publishes an event. The next service listens to that event and performs its local transaction.

### Two Types of Saga

| Type | How it works | Coordinator | Our Project |
|------|-------------|-------------|-------------|
| **Choreography** | Services listen to each other's events — no central controller | None (event-driven) | ✅ Yes |
| **Orchestration** | A central orchestrator tells each service what to do | Saga Orchestrator | ❌ No |

### Our Choreography-Based Saga Flow

```
Order Service                    Kafka                     Product Service
─────────────                    ─────                     ───────────────
1. Create Order (PENDING)
2. Publish "order-created" ────→ [order-created] ────→ 3. Receive event
                                                       4. Check stock
                                                       5a. Stock OK → reduce stock
                                                           Publish "order-success" ────→ [order-success]
                                                       5b. Stock LOW →
                                                           Publish "order-failed"  ────→ [order-failed]
6a. Receive "order-success" ←── [order-success]
    Update order → CONFIRMED
6b. Receive "order-failed"  ←── [order-failed]
    Update order → FAILED
```

### Product Service Role in Saga

Product Service is the **responder** in the saga. It:
1. Listens to `order-created` topic
2. Validates stock availability
3. Reduces stock if sufficient
4. Sends `order-success` or `order-failed` event back

```java
@KafkaListener(topics = "order-created", groupId = "product-service-group")
public void handleOrderCreated(OrderEvent orderEvent) {
    Product product = products.get(orderEvent.getProductId());
    
    if (product.getStock() >= orderEvent.getQuantity()) {
        product.setStock(product.getStock() - orderEvent.getQuantity());
        sendOrderSuccessEvent(orderEvent, product);   // ✅ Confirm
    } else {
        sendOrderFailedEvent(orderEvent, "Insufficient stock");  // ❌ Reject
    }
}
```

### Transactional Guarantees

| Property | How We Achieve It |
|----------|------------------|
| **Atomicity** | Each service has its own local transaction (order save, stock reduce) |
| **Consistency** | Saga ensures eventual consistency via compensating events |
| **Isolation** | PENDING status prevents reading uncommitted state |
| **Durability** | Kafka persists events; services persist local data |

**Key Concept — Eventual Consistency:**
The system is **not immediately consistent** (order is PENDING until Product Service responds). But it **eventually becomes consistent** when the success/failure event arrives.

---

## 22. Kafka Event Rollback

### What Happens When Something Fails?

In our saga, if Product Service finds insufficient stock, it doesn't "rollback" a database transaction — it sends a **compensating event** that tells Order Service to mark the order as FAILED.

### Rollback Flow (Compensating Transaction)

```
Order Service                       Kafka                    Product Service
─────────────                       ─────                    ───────────────
1. Order created (PENDING)
2. Send "order-created" ──────→ [order-created] ──────→ 3. Receive event
                                                         4. Check stock
                                                         5. Stock < required ❌
                                                         6. Send "order-failed" ──→ [order-failed]
7. Receive "order-failed" ←──── [order-failed]
8. order.setStatus("FAILED")  ← COMPENSATING ACTION
```

### How Product Service Triggers Rollback

```java
private void sendOrderFailedEvent(OrderEvent orderEvent, String reason) {
    OrderFailedEvent failedEvent = new OrderFailedEvent();
    failedEvent.setOrderId(orderEvent.getOrderId());
    failedEvent.setProductId(orderEvent.getProductId());
    failedEvent.setQuantity(orderEvent.getQuantity());
    failedEvent.setStatus("FAILED");
    failedEvent.setTimestamp(System.currentTimeMillis());
    failedEvent.setFailureReason(reason);  // "Insufficient stock" or "Product not found"
    
    kafkaTemplate.send(ORDER_FAILED_TOPIC, String.valueOf(orderEvent.getOrderId()), failedEvent);
}
```

### Failure Scenarios from Product Service Perspective

| Scenario | Product Service Action | Event Sent |
|----------|----------------------|------------|
| Product not found | Skip stock check | `order-failed` (reason: "Product not found") |
| Insufficient stock | Don't reduce stock | `order-failed` (reason: "Insufficient stock") |
| Stock available | Reduce stock | `order-success` |
| Product Service down | Nothing (service is down) | No event (Order stays PENDING) |

### Key Differences: DB Rollback vs Saga Rollback

| Aspect | Database Rollback | Saga Compensating Transaction |
|--------|------------------|-------------------------------|
| **Mechanism** | `ROLLBACK` SQL command undoes changes | New event triggers reverse action |
| **Timing** | Immediate (synchronous) | Eventually (asynchronous) |
| **Scope** | Single database | Across multiple services |
| **Intermediate State** | None (all-or-nothing) | PENDING state visible |
| **Complexity** | Built-in to DB | Developer must implement |

### Future Enhancement: Stock Rollback on Order Cancellation

If an order is cancelled **after** stock was reduced, Product Service would need a listener:

```
Order Service                      Kafka                    Product Service
─────────────                      ─────                    ───────────────
Cancel order (CONFIRMED→CANCELLED)
Send "order-cancelled" ──────→ [order-cancelled] ──────→ Receive event
                                                          Restore stock
                                                          product.stock += quantity
```

---

## 23. Confluent Cloud Kafka Setup

### Overview

Confluent Cloud is a **managed Apache Kafka service** that eliminates infrastructure management. This Product Service connects to Confluent Cloud for event-driven communication with Order Service.

### Prerequisites

✅ Confluent Cloud Account (Free tier available at https://confluent.cloud)
✅ Both services configured with KafkaConfig.java
✅ Spring Boot with spring-kafka dependency
✅ @EnableKafka annotation on Application class

---

### Quick Start: 5 Easy Steps

#### STEP 1: Create Confluent Cloud Account
```
1. Go to https://confluent.cloud
2. Sign up → Create Organization → Verify Email
3. Create Environment "development"
4. Create Cluster (Standard, us-east-2, Single Zone)
5. Wait 5-10 minutes for provisioning
```

#### STEP 2: Get API Credentials
```
Cluster Dashboard → API Keys → Create New
SAVE: API Key, API Secret, Bootstrap Server
(Won't be shown again!)
```

#### STEP 3: Create Kafka Topics
```
Cluster Dashboard → Topics → Create Topic

Topics to create:
  • order-created (3 partitions, 3 replication)
  • order-success (3 partitions, 3 replication)
  • order-failed  (3 partitions, 3 replication)
```

#### STEP 4: Update application-dev.properties
```properties
spring.kafka.bootstrap-servers=pkc-921jm.us-east-2.aws.confluent.cloud:9092
spring.kafka.properties.security.protocol=SASL_SSL
spring.kafka.properties.sasl.mechanism=PLAIN
spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="YOUR_API_KEY" password="YOUR_API_SECRET";
```

#### STEP 5: Run Service
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

---

### Detailed Configuration

#### File: application-dev.properties

```properties
# ===================================================================
# CONFLUENT CLOUD KAFKA CONFIGURATION
# ===================================================================

# Bootstrap Server (From Confluent Cloud Console > Cluster Settings)
spring.kafka.bootstrap-servers=pkc-921jm.us-east-2.aws.confluent.cloud:9092

# Security: SASL/SSL (Required for Confluent Cloud)
spring.kafka.properties.security.protocol=SASL_SSL
spring.kafka.properties.sasl.mechanism=PLAIN
spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="KOBXAXKO5FTSLBLB" password="cfltI+IDXbYrU2mqj3wKwsTEpAfWQVXrPlZ9wPjKNKYjPtVkfPrZEJ538QKZAWSA";

# Producer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3

# Consumer Configuration
spring.kafka.consumer.group-id=product-service-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.consumer.auto-offset-reset=earliest

# Kafka Topics
kafka.topic.order-created=order-created
kafka.topic.order-success=order-success
kafka.topic.order-failed=order-failed
```

#### File: ProductApplication.java

```java
@SpringBootApplication
@EnableKafka          // ✅ REQUIRED
@EnableAsync
@EnableCaching
@EnableScheduling
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
```

#### File: KafkaConfig.java

```java
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "product-service-group");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
    }
}
```

---

### Testing & Verification

#### Start Service
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### Expected Log Output
```
Sending Heartbeat request to coordinator
Received HEARTBEAT response from node
Received successful Heartbeat response
✅ CONNECTED TO CONFLUENT CLOUD!
```

#### Monitor in Confluent Cloud Console
```
1. Cluster Dashboard > Topics > order-created
2. Watch messages flowing in real-time
3. Check consumer group lag
4. Verify partition distribution
```

---

### Troubleshooting

| Problem | Solution |
|---------|----------|
| Connection timeout | Verify bootstrap server, check firewall |
| Auth failed | Copy new API key from console |
| Topic not found | Create topics manually in console |
| No messages | Check producer/consumer config |

---

## 24. RestTemplate - Sync Communication

### What is RestTemplate?

`RestTemplate` is a Spring HTTP client used for **synchronous** (blocking) REST API calls between microservices. In our project, Order Service uses RestTemplate to call Product Service's `/v1/products/{id}` endpoint before creating an order.

### Why Does Order Service Call Product Service Synchronously?

```
Order Service                                   Product Service
─────────────                                   ───────────────
1. REST Call (sync) ──── GET /v1/products/101 ──→ Return product details
2. Validate product exists & get price
3. Create order with PENDING status
4. Send Kafka event (async) ──────────────────→ Process stock
```

**Why not just Kafka for everything?**
- Need immediate validation (does the product exist?)
- Need the product price to create the order
- User expects instant feedback ("order created" or "product not found")
- Kafka is fire-and-forget — can't return data synchronously

### Product Service Endpoint Called by RestTemplate

```java
// ProductController.java — endpoint that RestTemplate calls
@GetMapping("/{id}")
public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
    return ResponseEntity.ok(productService.getProductById(id));
}
```

When Order Service calls `GET http://localhost:8080/v1/products/101`, it hits this endpoint and gets back a `ProductResponse` with id, name, price, stock, etc.

### Common RestTemplate Methods

| Method | HTTP Verb | Use Case |
|--------|-----------|----------|
| `getForObject()` | GET | Fetch data (used by Order Service) |
| `getForEntity()` | GET | Fetch data + headers + status |
| `postForObject()` | POST | Create resource, get response |
| `put()` | PUT | Update resource |
| `delete()` | DELETE | Delete resource |
| `exchange()` | ANY | Full control (headers, method, body) |

### RestTemplate vs WebClient vs OpenFeign

| Feature | RestTemplate | WebClient | OpenFeign |
|---------|-------------|-----------|-----------|
| **Blocking** | Yes (synchronous) | No (async/reactive) | Yes |
| **Spring version** | All versions | WebFlux (reactive) | Spring Cloud |
| **Ease of use** | Simple | Complex | Very simple |
| **Performance** | Blocks thread per call | Non-blocking I/O | Blocks thread |
| **Our choice** | ✅ Simple, sufficient | Overkill for 2 services | Needs Spring Cloud |

---

## 25. Resilience4j - Fault Tolerance

| Pattern | Where Used | Purpose |
|---------|-----------|---------|
| **CircuitBreaker** | `handleOrderCreated()` (Kafka) | Protects Kafka consumer from cascading failures |
| **Retry** | Configurable | Retries failed operations |
| **RateLimiter** | `getAllProducts()` | Limits API calls to 100/sec |
| **Bulkhead** | `getProductById()` | Max 20 concurrent calls |

**Fallback with Custom Exception:**
```java
@CircuitBreaker(name = "kafkaProcessor", fallbackMethod = "kafkaProcessorFallback")
public void handleOrderCreated(OrderCreatedEvent event) { ... }

private void kafkaProcessorFallback(OrderCreatedEvent event, Throwable t) {
    // Sends order-failed Kafka event as compensation
    kafkaTemplate.send("order-failed", failedEvent);
}
```

**Configuration (`application-dev.properties`):**
```properties
resilience4j.circuitbreaker.instances.kafkaProcessor.sliding-window-size=10
resilience4j.circuitbreaker.instances.kafkaProcessor.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.kafkaProcessor.wait-duration-in-open-state=10s
resilience4j.ratelimiter.instances.productApi.limit-for-period=100
resilience4j.bulkhead.instances.productApi.max-concurrent-calls=20
```

---

## 26. @Async - Asynchronous Processing

```java
@Configuration @EnableAsync
public class AsyncCacheSchedulingConfig {
    @Bean(name = "productTaskExecutor")
    public Executor productTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("product-async-");
        return executor;
    }
}

@Async("productTaskExecutor")
public CompletableFuture<List<ProductResponse>> getAllProductsAsync() { ... }
```

**Endpoint:** `GET /v1/products/async`

---

## 27. @Cacheable - Spring Cache

```java
@EnableCaching

@Cacheable(value = "products", key = "'all'")
public List<ProductResponse> getAllProducts() { ... }

@Cacheable(value = "productById", key = "#id")
public ProductResponse getProductById(Long id) { ... }

@CacheEvict(value = "products", allEntries = true)
public ProductResponse createProduct(ProductRequest req) { ... }
```

---

## 28. @Scheduled - Task Scheduling

```java
@EnableScheduling

@Scheduled(fixedRateString = "${product.service.scheduling-rate-ms:60000}")
public void logProductStats() {
    // Logs total products and low-stock count every 60s
}
```

---

## 29. API Versioning

```
/v1/products    → Original API (full features)
/v2/products    → New API (pagination by default)
```

---

## 30. Pagination

```java
public PagedResponse<ProductResponse> getProductsPaged(int page, int size) { ... }
```

**Request:** `GET /v1/products/paged?page=0&size=5`

**Response:**
```json
{
  "content": [ ... ],
  "page": 0, "size": 5,
  "totalElements": 50, "totalPages": 10
}
```

---

## 31. Dockerization

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
```

**Build & Run:**
```bash
docker build -t product-service .
docker run -p 8080:8080 product-service
```

---

## 32. Testing and Troubleshooting

### Run Tests
```bash
mvn test
```

### Test Profile
- Uses `application-test.properties`
- In-memory database (auto-reset)
- Port: 8082
- Log Level: WARN

### Troubleshooting

### Port Already in Use
```bash
# Find process using port 8081
lsof -i :8081

# Kill process
kill <PID>
```

### Properties Not Loading
- Check `spring.profiles.active` in `application.properties`
- Verify correct profile file exists: `application-{profile}.properties`
- Check property names (case-sensitive)

### Swagger UI Not Accessible
- Ensure Spring Boot is running
- Check URL: `http://localhost:{port}/swagger-ui.html`
- Check logs for startup errors

---

## 33. Future Enhancements

* Add database (JPA + MySQL)
* Add Spring Security (JWT)
* Add Redis for distributed caching
* Add Micrometer + Prometheus metrics
* Add docker-compose for multi-service orchestration

---

## Interview Explanation

> "I built a Spring Boot-based Product Service using a layered architecture. I followed API-first design, used DTOs with validation, implemented structured logging with correlationId using MDC, added interceptors for request tracing, integrated Swagger for API documentation, and handled exceptions globally. I also added Resilience4j fault tolerance, async processing, caching, scheduling, pagination, API versioning, and Dockerization."

---

## Conclusion

This project demonstrates:

* Clean coding practices
* Production-level logging
* Observability
* API documentation
* Fault tolerance (Resilience4j)
* Async, Caching, Scheduling
* API Versioning & Pagination
* Dockerization

👉 Fully **interview-ready backend system** 🚀

---

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Springdoc OpenAPI](https://springdoc.org/)
- [Lombok Documentation](https://projectlombok.org/)
- [Jakarta Validation](https://jakarta.ee/specifications/bean-validation/)
