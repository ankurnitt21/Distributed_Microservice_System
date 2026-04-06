# Order Service - Microservice Architecture

## Table of Contents

| # | Topic |
|---|-------|
| 1 | [Overview](#1-overview) |
| 2 | [Technology Stack](#2-technology-stack) |
| 3 | [Project Structure](#3-project-structure) |
| 4 | [How to Run](#4-how-to-run) |
| 5 | [REST API Endpoints](#5-rest-api-endpoints) |
| 6 | [Configuration Management](#6-configuration-management-value) |
| 7 | [@ConfigurationProperties](#7-configurationproperties---type-safe-config) |
| 8 | [Configuration Summary](#8-configuration-summary) |
| 9 | [Spring IoC and Dependency Injection](#9-spring-ioc-and-dependency-injection) |
| 10 | [Bean Scoping Strategy](#10-bean-scoping-strategy) |
| 11 | [Spring Bean Lifecycle](#11-spring-bean-lifecycle) |
| 12 | [Prototype Beans for Multi-Threading](#12-prototype-beans-for-multi-threading) |
| 13 | [Validation and Error Handling](#13-validation-and-error-handling) |
| 14 | [Logging and Request Tracing (MDC)](#14-logging-and-request-tracing-mdc) |
| 15 | [MDC Propagation Across Threads](#15-mdc-propagation-across-threads) |
| 16 | [Request Flow](#16-request-flow---how-everything-works-together) |
| 17 | [Swagger/OpenAPI Documentation](#17-swaggeropenapi-documentation) |
| 18 | [@Conditional + @Profile + @Qualifier](#18-conditional--profile--qualifier) |
| 19 | [Spring Boot DevTools](#19-spring-boot-devtools) |
| 20 | [Spring Boot Actuator](#20-spring-boot-actuator) |
| 21 | [Distributed Saga Pattern with Kafka](#21-distributed-saga-pattern-with-kafka) |
| 22 | [Kafka Event Rollback](#22-kafka-event-rollback---compensating-transactions) |
| 23 | [Confluent Cloud Kafka Setup](#23-confluent-cloud-kafka-setup) |
| 24 | [RestTemplate - Sync Communication](#24-resttemplate---sync-communication) |
| 25 | [Resilience4j - Fault Tolerance](#25-resilience4j---fault-tolerance) |
| 26 | [@Async - Asynchronous Processing](#26-async---asynchronous-processing) |
| 27 | [@Cacheable - Spring Cache](#27-cacheable---spring-cache) |
| 28 | [@Scheduled - Task Scheduling](#28-scheduled---task-scheduling) |
| 29 | [API Versioning](#29-api-versioning) |
| 30 | [Pagination](#30-pagination) |
| 31 | [Dockerization](#31-dockerization) |
| 32 | [Design Patterns Summary](#32-design-patterns-summary) |
| 33 | [Future Enhancements](#33-future-enhancements) |

---


## 1. Overview
Order Service is a Spring Boot REST microservice built with enterprise-grade design patterns, logging, and configuration management. It demonstrates best practices for microservice development including dependency injection, bean scoping, request-scoped tracing, and multi-environment profiles.

---

## 2. Technology Stack
- **Framework:** Spring Boot 4.0.5
- **Java Version:** 17
- **Build Tool:** Maven
- **API Documentation:** Swagger/OpenAPI 3.0
- **Logging:** SLF4J + Logback
- **Dependency Injection:** Spring IoC
- **Data Storage:** In-memory HashMap (can be replaced with DB)

---

## 3. Project Structure
```
src/main/
├── java/com/example/order/order/
│   ├── OrderApplication.java              # Entry point
│   ├── model/
│   │   └── Order.java                     # Order entity (@Data, @Component)
│   ├── dto/
│   │   ├── OrderRequest.java              # Request DTO with validation
│   │   ├── OrderResponse.java             # Response DTO
│   │   └── ErrorResponse.java             # Error response wrapper
│   ├── controller/
│   │   └── OrderController.java           # REST endpoints (@RestController)
│   ├── service/
│   │   ├── OrderService.java              # Business logic (@Service, Singleton)
│   │   └── OrderAnalyticsService.java     # Analytics service (@Lazy initialization)
│   ├── Exceptions/
│   │   ├── OrderNotFoundException.java     # Custom exception
│   │   └── GlobalExceptionHandler.java    # @ControllerAdvice for error handling
│   └── config/
│       ├── OrderProcessingContext.java    # Prototype-scoped request context
│       ├── LoggingInterceptor.java        # HandlerInterceptor for tracing
│       ├── SwaggerConfig.java             # OpenAPI configuration
│       ├── WebConfig.java                 # Spring MVC interceptor registration
│       └── AppConfig.java                 # @Value properties bean
└── resources/
    ├── application.properties               # Default profile
    ├── application-dev.properties          # Development profile
    ├── application-prod.properties         # Production profile
    ├── application-test.properties         # Test profile
    └── logback-spring.xml                  # Logging configuration
```

---

## 4. How to Run

### Build
```bash
mvn clean package
```

### Run (Default - Dev Profile)
```bash
mvn spring-boot:run
```

### Run with Specific Profile
```bash
# Production profile
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod

# Test profile
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=test
```

### Run JAR
```bash
java -Dspring.profiles.active=prod -jar target/order-0.0.1-SNAPSHOT.jar
```

---

## 5. REST API Endpoints

### v1 Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/orders` | Create new order (syncs with Product Service via REST, sends Kafka event) |
| GET | `/v1/orders` | Get all orders (optional `?name=` filter, RateLimiter + Cacheable) |
| GET | `/v1/orders/{id}` | Get order by ID (Bulkhead + Cacheable) |
| GET | `/v1/orders/paged` | Get orders with pagination (`?page=0&size=10`) |
| GET | `/v1/orders/async` | Get all orders asynchronously (returns `CompletableFuture`) |
| GET | `/v1/orders/analytics/report` | Get analytics report (triggers @Lazy initialization) |
| GET | `/v1/orders/{id}/analytics` | Get statistics for specific order |

### v2 Endpoints (API Versioning)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v2/orders` | Get orders with pagination by default (`?page=0&size=10`) |
| GET | `/v2/orders/{id}` | Get order by ID |

**Example Requests:**

```bash
# Create order
curl -X POST http://localhost:8081/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: abc-123" \
  -H "X-Employee-ID: emp-456" \
  -d '{
    "name": "Laptop",
    "description": "Dell XPS 15",
    "price": 150000,
    "quantity": 2
  }'

# Get all orders
curl http://localhost:8081/v1/orders

# Get orders with name filter
curl http://localhost:8081/v1/orders?name=Laptop

# Get order by ID
curl http://localhost:8081/v1/orders/1

# Get analytics report (triggers lazy initialization of OrderAnalyticsService)
curl http://localhost:8081/v1/orders/analytics/report

# Get statistics for order ID 1
curl http://localhost:8081/v1/orders/1/analytics
```

---

## 6. Configuration Management (@Value)

Properties defined in profile files and injected via `@Value`:

**AppConfig.java:**
```java
@Value("${app.name}")           // Order Service
@Value("${app.version}")        // 1.0.0-DEV (profile-specific)
@Value("${app.max-orders}")     // 1000 (dev), 5000 (prod), 100 (test)
@Value("${server.port}")        // 8081 (dev), 8080 (prod), 8082 (test)
```

**Profile Files:**
- `application-dev.properties` — Local development (port 8081, DEBUG logs)
- `application-prod.properties` — Production (port 8080, WARN logs)
- `application-test.properties` — Testing (port 8082, INFO logs)

**To activate a profile:**
```bash
java -Dspring.profiles.active=dev -jar application.jar
# or via environment variable
export SPRING_PROFILES_ACTIVE=prod
java -jar application.jar
```

---

## 7. @ConfigurationProperties - Type-Safe Config

```java
@Data
@Component
@ConfigurationProperties(prefix = "order.service")
public class OrderServiceProperties {
    private String productServiceUrl = "http://localhost:8080/v1/products/";
    private int pageSize = 10;
    private boolean asyncEnabled = true;
    private boolean cacheEnabled = true;
    private long schedulingRateMs = 60000;
}
```

**Usage:** `properties.getProductServiceUrl()` instead of hardcoded URLs.

---

## 8. Configuration Summary

### application.properties (Default/Base)
```properties
spring.application.name=order
logging.level.org.springframework.web=DEBUG
server.port=8081
app.name=Order Service
app.version=1.0.0
app.max-orders=1000
```

### application-dev.properties
```properties
spring.profiles.active=dev
server.port=8081
logging.level.org.springframework.web=DEBUG
app.max-orders=1000
```

### application-prod.properties
```properties
spring.profiles.active=prod
server.port=8080
logging.level.org.springframework.web=WARN
app.max-orders=5000
```

---

## 9. Spring IoC and Dependency Injection

**How beans are registered and injected:**

1. **Constructor Injection** (preferred):
   ```java
   @RestController
   public class OrderController {
       private OrderService orderService;
       
       public OrderController(OrderService orderService) {
           this.orderService = orderService;
       }
   }
   ```

2. **Field Injection** (with @Autowired):
   ```java
   @Service
   public class OrderService {
       @Autowired
       private ObjectProvider<OrderProcessingContext> contextProvider;
   }
   ```

3. **ObjectProvider** (for Prototype/Request-scoped beans):
   ```java
   @Autowired
   private ObjectProvider<OrderProcessingContext> ctxProvider;
   
   OrderProcessingContext ctx = ctxProvider.getObject(); // new instance
   ```

---

## 10. Bean Scoping Strategy

#### Singleton Beans (Stateless, Shared)
- `OrderService` — Singleton service with in-memory order storage
- `OrderController` — HTTP request routing (stateless)
- `LoggingInterceptor` — Request/response interceptor (stateless, thread-safe)
- `AppConfig` — Configuration holder (@Value properties)
- `SwaggerConfig` — API documentation setup

**Why Singleton?**
- Efficient memory usage (one instance per application)
- Safe for concurrent requests (stateless)
- Shared data (orders HashMap) across all requests

#### Prototype Beans (Request-Scoped)
- `OrderProcessingContext` — Per-request context with timing data
  - `@Scope(BeanDefinition.SCOPE_PROTOTYPE)` — new instance per request
  - Stores: `startTime`, `duration`
  - Created in `LoggingInterceptor.preHandle()` via `ObjectProvider<OrderProcessingContext>`
  - Discarded after request completion (no memory leaks)

**Why Prototype?**
- Each request gets its own isolated context
- Timing data cannot leak between requests
- Per-request state is safely encapsulated

#### Lazy-Initialized Beans (@Lazy)
- `OrderAnalyticsService` — Analytics service with expensive initialization
  - `@Service @Lazy` — Bean created only when first accessed (not at startup)
  - `@PostConstruct` initialization includes: expensive I/O, external API calls, model loading
  - Accessed via `ObjectProvider<OrderAnalyticsService>` to ensure lazy initialization
  - Optional service — used only when analytics endpoints are called

**Why @Lazy?**
- ✅ Improves application startup time (2+ seconds saved in this example)
- ✅ Resources allocated only when actually needed
- ✅ Useful for optional features, heavy computations, external service integrations
- ✅ Reduces memory footprint if service is rarely used
- ✅ Non-blocking initialization — fails gracefully if initialization is optional

**When to use @Lazy:**
- Heavy database migrations or connection pooling
- External service clients (payment gateway, email API, analytics platforms)
- Machine learning model loading
- Cache initialization
- Report generators (rarely used, computationally expensive)
- Email/SMS service clients
- Third-party API integrations

**Real-world Use Case in OrderAnalyticsService:**

```java
@Service
@Lazy  // Not initialized at startup
public class OrderAnalyticsService {
    
    @PostConstruct
    public void init() {
        // Expensive operations (2+ seconds):
        // - Connect to analytics database
        // - Load ML model for predictions
        // - Initialize cache
        // - Call external analytics API
    }
    
    public AnalyticsReport generateReport() { ... }
}
```

**How it's used in OrderController:**

```java
@RestController
public class OrderController {
    private ObjectProvider<OrderAnalyticsService> analyticsServiceProvider;
    
    @PostMapping  // Regular create order
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest req) {
        OrderResponse response = orderService.createOrder(req);
        
        // Analytics is optional - only accessed if needed
        OrderAnalyticsService analytics = analyticsServiceProvider.getIfAvailable();
        if (analytics != null) {
            analytics.recordOrderCreated(response.getId(), response.getName(), response.getPrice());
        }
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/analytics/report")  // Analytics endpoint
    public ResponseEntity<AnalyticsReport> getAnalyticsReport() {
        // Accessing this endpoint triggers @Lazy initialization (first access only)
        OrderAnalyticsService analytics = analyticsServiceProvider.getObject();
        return ResponseEntity.ok(analytics.generateReport());
    }
}
```

**Startup Time Comparison:**

| Scenario | Startup Time | Notes |
|----------|--------------|-------|
| **Without @Lazy** | ~5-7 seconds | OrderAnalyticsService initializes at startup (2s) + other beans (3-5s) |
| **With @Lazy** | ~3-5 seconds | OrderAnalyticsService skipped, initialized only when `/analytics/report` is called |
| **First Analytics Call** | +2 seconds | One-time delay when analytics endpoint accessed for first time |
| **Subsequent Calls** | Instant | Service already initialized, subsequent calls have no delay |

**Key Points:**
- `@Lazy` requires `ObjectProvider<T>` injection (not direct `@Autowired`)
- Use `getIfAvailable()` for optional services (returns null if not used)
- Use `getObject()` for required services (initializes if not already done)
- @Lazy beans are still Singletons (created once, cached after initialization)
- Thread-safe initialization (Spring handles concurrent access)

---

## 11. Spring Bean Lifecycle

Every Spring bean goes through a well-defined lifecycle from creation to destruction. Understanding this lifecycle is crucial for proper resource management and initialization order.

#### Complete Bean Lifecycle Sequence

```
1. INSTANTIATION
   └─ Constructor called (new instance created)
      
2. DEPENDENCY INJECTION
   └─ Setter injection / Constructor parameters injected
   
3. AWARE INTERFACES (in order)
   ├─ BeanNameAware.setBeanName()        → Bean knows its name
   ├─ BeanFactoryAware.setBeanFactory()  → Bean can access BeanFactory
   ├─ ApplicationContextAware.setApplicationContext() → Bean can access ApplicationContext
   
4. PRE-INITIALIZATION PROCESSING
   └─ BeanPostProcessor.postProcessBeforeInitialization()
   
5. INITIALIZATION (choose ONE, not all)
   ├─ @PostConstruct method
   ├─ InitializingBean.afterPropertiesSet()
   └─ @Bean(initMethod="...")
   
6. POST-INITIALIZATION PROCESSING
   └─ BeanPostProcessor.postProcessAfterInitialization()
   
7. READY FOR USE
   └─ Bean is singleton and cached in context
   
8. PRE-DESTRUCTION (on shutdown)
   └─ BeanPostProcessor.postProcessAfterInitialization() [some]
   
9. DESTRUCTION (choose ONE, not all)
   ├─ @PreDestroy method
   ├─ DisposableBean.destroy()
   └─ @Bean(destroyMethod="...")
   
10. DESTROYED
    └─ Bean removed from context
```

#### BeanFactoryPostProcessor vs BeanPostProcessor

**BeanFactoryPostProcessor:**
- Executes **BEFORE** any bean instantiation
- Modifies bean definitions (metadata)
- Single-threaded execution
- Used once at startup
- Modify classes, properties, scope of beans

**BeanPostProcessor:**
- Executes **AFTER** bean instantiation but within lifecycle
- Wraps/enhances actual bean instances
- Called for every bean
- Can be multi-threaded
- Add behavior to beans via proxying

```java
// BEFORE any bean is created (BeanFactoryPostProcessor)
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) {
        // Modify OrderService bean definition
        // Change scope from Singleton to Prototype
        // Add lazy initialization flag
    }
}

// AFTER each bean is created (BeanPostProcessor)
public class CustomBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String name) {
        // Before @PostConstruct
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String name) {
        // After afterPropertiesSet() / InitializingBean
        // Can wrap bean in proxy here (AOP)
    }
}
```

#### ApplicationContext vs BeanFactory

**BeanFactory (Low-level):**
- Basic container interface
- Lazy initialization by default
- Minimal features
- Used internally by Spring
- Better for resource-constrained environments

**ApplicationContext (High-level):**
- Extends BeanFactory
- Eager singleton initialization
- Additional features (events, properties, i18n, AOP)
- Recommended for most applications
- This is what you typically use in Spring Boot

```java
// BeanFactory Example
@Component
public class ServiceA implements BeanFactoryAware {
    private BeanFactory factory;
    
    @Override
    public void setBeanFactory(BeanFactory factory) {
        this.factory = factory;
    }
    
    public void demonstrateFactory() {
        // Get bean by name
        ServiceB serviceB = (ServiceB) factory.getBean("serviceB");
        
        // Check if bean exists
        boolean exists = factory.containsBean("serviceB");
        
        // Get bean type
        Class<?> type = factory.getType("serviceB");
        
        // Check if singleton
        boolean isSingleton = factory.isSingleton("serviceB");
    }
}

// ApplicationContext Example
@Component
public class ApplicationContextDemo {
    private final ApplicationContext context;
    
    public ApplicationContextDemo(ApplicationContext context) {
        this.context = context;
    }
    
    public void demonstrateApplicationContext() {
        // More convenient getBean with type
        ServiceB serviceB = context.getBean(ServiceB.class);
        
        // Get all beans of a type
        Map<String, ServiceB> allBeans = context.getBeansOfType(ServiceB.class);
        
        // Get all bean names
        String[] beanNames = context.getBeanDefinitionNames();
        
        // Access environment and properties
        String appName = context.getEnvironment().getProperty("spring.application.name");
        
        // Publish events
        context.publishEvent(new MyCustomEvent(this));
    }
}
```

#### Real-world Example: OrderServiceLifecycleBean in Project

Our project includes `BeanLifecycleDemo.java` that demonstrates:

```
1. Constructor called
   └─ @Component triggers instantiation

2. Setter injection
   └─ serviceId property set

3. Aware interfaces
   ├─ BeanNameAware: bean knows name = "orderServiceLifecycleBean"
   ├─ BeanFactoryAware: bean gains access to BeanFactory
   └─ ApplicationContextAware: bean gains access to ApplicationContext

4. BeanPostProcessor.preInitialization()
   └─ CustomBeanPostProcessor runs

5. @PostConstruct
   └─ postConstruct() method called

6. InitializingBean.afterPropertiesSet()
   └─ Expensive initialization (DB, API calls)

7. BeanPostProcessor.postInitialization()
   └─ CustomBeanPostProcessor runs again

8. READY FOR USE
   └─ Bean can now be injected into other beans

[Later, on application shutdown]

9. @PreDestroy
   └─ preDestroy() method called

10. DisposableBean.destroy()
    └─ Cleanup resources, close connections
```

**When to use each lifecycle method:**

| Method | When | Use Case |
|--------|------|----------|
| **Constructor** | Instance creation | Mandatory initialization |
| **@PostConstruct** | After properties set | Validate properties, start threads, connect to DB |
| **InitializingBean** | After properties set | Legacy code, template methods |
| **@PreDestroy** | Before shutdown | Close connections, release resources |
| **DisposableBean** | Before shutdown | Legacy cleanup code |
| **BeanPostProcessor** | Around initialization | AOP, proxying, wrapping beans |
| **BeanFactoryPostProcessor** | Before instantiation | Modify bean definitions |

**Best Practices:**

- ✅ Use `@PostConstruct` / `@PreDestroy` (modern, cleaner)
- ✅ Use ApplicationContext (not BeanFactory) in most cases
- ✅ Implement BeanFactoryPostProcessor sparingly (advanced)
- ✅ Use BeanPostProcessor for AOP and proxy creation
- ✅ Keep initialization lightweight (move heavy work to separate thread)
- ❌ Avoid accessing other beans in constructor (circular references)
- ❌ Don't store request-scoped state in singleton beans
- ❌ Don't do expensive operations in constructor (lazy init instead)

---

## 12. Prototype Beans for Multi-Threading

When business logic runs in **multiple threads**, Singleton beans can cause **race conditions** if they store stateful data. In such cases, use **Prototype scope** with **context sharing**:

#### Scenario: Async Order Processing with Context Sharing

---

## 13. Validation and Error Handling

**OrderRequest DTO Validation:**
```java
@NotBlank               // name cannot be blank
@Size(min=1, max=100)   // name length constraint
@DecimalMin("100.0")    // price >= 100
@Min(1)                 // quantity >= 1
```

**GlobalExceptionHandler:**
- Uses `@RestControllerAdvice` (REST-specific exception handling)
- Handles `OrderNotFoundException` (404)
- Handles generic exceptions (500)
- Returns structured `ErrorResponse` with message, status, timestamp

#### @RestControllerAdvice vs @ControllerAdvice

**Difference:**

| Aspect | @ControllerAdvice | @RestControllerAdvice |
|--------|------------------|----------------------|
| **What is it?** | General-purpose exception handling for controllers | REST API-specific exception handling |
| **Return Type** | Model & View, String (template names), ResponseEntity, @ResponseBody | Always JSON/XML (serialized objects) |
| **Serialization** | Requires `@ResponseBody` on handler methods | Automatic serialization via HttpMessageConverter |
| **Use Case** | Traditional web applications (MVC with JSP/Thymeleaf) | REST APIs (JSON responses) |
| **Annotation Combo** | `@ControllerAdvice` + `@ResponseBody` | `@RestControllerAdvice` (equivalent to both) |

**When to use @ControllerAdvice:**
```java
// Traditional web application with HTML templates
@ControllerAdvice
public class WebExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody  // Need this to return JSON
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public String handleValidation(ValidationException ex, Model model) {
        // Return template name for error page
        model.addAttribute("error", ex.getMessage());
        return "error-page";  // Thymeleaf template name
    }
}
```

**When to use @RestControllerAdvice (Our Project):**
```java
// REST API - always returns JSON
@RestControllerAdvice  // = @ControllerAdvice + @ResponseBody on all methods
public class GlobalExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        // Automatic JSON serialization - no @ResponseBody needed
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setStatus(404);
        errorResponse.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(404).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // Always returns JSON, never HTML
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("An unexpected error occurred: " + ex.getMessage());
        errorResponse.setStatus(500);
        errorResponse.setTimestamp(System.currentTimeMillis());
        return ResponseEntity.status(500).body(errorResponse);
    }
}
```

**Real-world Comparison Example:**

Request to non-existent order: `GET /v1/orders/999`

**With @RestControllerAdvice (Our Project):**
```json
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "message": "Order not found with id: 999",
  "status": 404,
  "timestamp": 1712293845000
}
```

**With @ControllerAdvice (without @ResponseBody):**
```
HTTP/1.1 404 Not Found
Content-Type: text/html

Could not resolve view with name 'error-page'  // Template not found!
```

**With @ControllerAdvice + @ResponseBody:**
```json
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "message": "Order not found with id: 999",
  "status": 404,
  "timestamp": 1712293845000
}
```

**Key Takeaways:**
- ✅ Use `@RestControllerAdvice` for **REST APIs** (JSON responses)
- ✅ Use `@ControllerAdvice` for **web applications** (HTML templates)
- ✅ `@RestControllerAdvice` = `@ControllerAdvice` + automatic `@ResponseBody`
- ✅ Always use `ResponseEntity<T>` for consistent status code + body handling
- ✅ Create domain-specific error DTOs (like `ErrorResponse` here) for consistency

---

## 14. Logging and Request Tracing (MDC)

**How it works:**
1. **LoggingInterceptor** (`preHandle`) reads headers:
   - `X-Correlation-ID` (or generates UUID if missing)
   - `X-Employee-ID` (or defaults to "Unknown")
2. Calls `MDC.put()` to store in thread-local map
3. Creates `OrderProcessingContext` prototype, sets `startTime`
4. All logs during request automatically include correlation/employee ID via logback pattern
5. **LoggingInterceptor** (`afterCompletion`) computes `duration`, clears MDC

**Log Pattern** (`logback-spring.xml`):
```
%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - [correlationId=%X{correlationId}] [employeeId=%X{employeeId}] %msg%n
```
Every log includes `correlationId` and `employeeId` automatically for request tracing.

**Log Levels by Profile:**
- **dev**: DEBUG
- **prod**: WARN
- **test**: INFO

---

## 15. MDC Propagation Across Threads

**Problem:** MDC is thread-local. When you spawn new threads (async operations), child threads **do NOT inherit** MDC values from the parent thread.

**Example Problem:**
```java
// Parent thread has MDC set: correlationId=abc-123
MDC.put("correlationId", "abc-123");

// Spawn new thread
new Thread(() -> {
    log.info("Child thread log");  
    // ❌ MDC is empty! Log has NO correlationId!
}).start();
```

#### Solution 1: Manual MDC Copy

```java
// In parent thread - capture MDC values
Map<String, String> mdcContext = MDC.getCopyOfContextMap();

// Spawn child thread - restore MDC
new Thread(() -> {
    MDC.setContextMap(mdcContext);  // Restore parent's MDC
    try {
        log.info("Child thread with correlation");  // ✅ Has correlationId
    } finally {
        MDC.clear();  // Cleanup
    }
}).start();
```

#### Solution 2: Wrap Runnable with MDC (Recommended for custom threads)

```java
public class MdcAwareRunnable implements Runnable {
    private final Runnable delegate;
    private final Map<String, String> mdcContext;
    
    public MdcAwareRunnable(Runnable delegate) {
        this.delegate = delegate;
        this.mdcContext = MDC.getCopyOfContextMap();  // Capture from parent
    }
    
    @Override
    public void run() {
        MDC.setContextMap(mdcContext);  // Restore in child
        try {
            delegate.run();
        } finally {
            MDC.clear();
        }
    }
}

// Usage
executorService.submit(new MdcAwareRunnable(() -> {
    log.info("Async task with correlation");  // ✅ Has correlationId
}));
```

#### Solution 3: Spring TaskDecorator (Best for Spring applications)

```java
@Configuration
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        
        // Decorate all tasks to propagate MDC automatically
        executor.setTaskDecorator(new MdcTaskDecorator());
        
        executor.initialize();
        return executor;
    }
}

// TaskDecorator implementation
public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return () -> {
            MDC.setContextMap(mdcContext);
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
```

#### Solution 4: Using Callable with MDC for Future-based tasks

```java
public class MdcAwareCallable<V> implements Callable<V> {
    private final Callable<V> delegate;
    private final Map<String, String> mdcContext;
    
    public MdcAwareCallable(Callable<V> delegate) {
        this.delegate = delegate;
        this.mdcContext = MDC.getCopyOfContextMap();
    }
    
    @Override
    public V call() throws Exception {
        MDC.setContextMap(mdcContext);
        try {
            return delegate.call();
        } finally {
            MDC.clear();
        }
    }
}

// Usage
Future<String> result = executorService.submit(
    new MdcAwareCallable<>(() -> {
        log.info("Processing with correlation");
        return "Result";
    })
);
```

#### Complete Practical Example with OrderService:

```java
@Service
public class OrderService {
    @Autowired
    private Executor asyncExecutor;  // TaskDecorator configured executor
    
    public void processOrderAsync(Long orderId) {
        // MDC is already set by LoggingInterceptor
        String correlationId = MDC.get("correlationId");  // e.g., "abc-123"
        String employeeId = MDC.get("employeeId");        // e.g., "emp-456"
        
        // Submit async task using the decorated executor
        // MDC will be automatically propagated via TaskDecorator
        asyncExecutor.execute(() -> {
            // Child thread already has MDC context via TaskDecorator
            log.info("Processing order {} asynchronously", orderId);
            // Output: 
            // 2026-04-05 10:30:45 [pool-1-thread-1] INFO OrderService - 
            // [correlationId=abc-123] [employeeId=emp-456] Processing order 1 asynchronously
            
            Order order = getOrder(orderId);
            validateOrder(order);
            persistOrder(order);
            
            log.info("Order {} processed successfully", orderId);
        });
    }
}
```

**Comparison of MDC Propagation Solutions:**

| Solution | Complexity | Scope | Performance | Recommended |
|----------|-----------|-------|-------------|-------------|
| Manual Copy | Low | Ad-hoc threads | Good | Small projects |
| MdcAwareRunnable | Low-Med | Custom thread pools | Good | Moderate projects |
| TaskDecorator | Medium | Spring `Executor` | Good | Large Spring apps |
| MdcAwareCallable | Low-Med | Future-based | Good | Async calls with return |

**Key Takeaways for MDC in Multi-threaded Environments:**
- ✅ Always capture MDC before spawning threads: `MDC.getCopyOfContextMap()`
- ✅ Restore MDC in child thread try block: `MDC.setContextMap(context)`
- ✅ Clear MDC in finally block to prevent leaks: `MDC.clear()`
- ✅ Use Spring `TaskDecorator` for automatic propagation in Spring apps
- ✅ All log statements in child threads will include correlation/employee IDs
- ✅ Wrap Callables for operations that return values
- ✅ Use `@Async` annotation with TaskDecorator for cleaner code

---

## 16. Request Flow - How Everything Works Together

This is the **complete detailed request flow** from client to server and back, covering every layer the request passes through.

---

### 16.1 Complete Request Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT (curl / Postman / Browser)                   │
│  POST /v1/orders                                                                 │
│  Headers: X-Correlation-ID: abc-123, X-Employee-ID: emp-456                      │
│  Body: { "productId": 1, "name": "Laptop", "price": 0, "quantity": 2 }          │
└──────────────────────────────────┬───────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│  1. TOMCAT EMBEDDED SERVER (Port 8081)                                           │
│     Accepts TCP connection, parses HTTP request, creates                          │
│     HttpServletRequest & HttpServletResponse objects                              │
│     Assigns a worker thread from Tomcat thread pool                               │
└──────────────────────────────────┬───────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│  2. DISPATCHER SERVLET                                                           │
│     Maps URL /v1/orders → OrderController.createOrder()                          │
│     But BEFORE reaching the controller, interceptors run first ↓                 │
└──────────────────────────────────┬───────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│  3. LOGGING INTERCEPTOR — preHandle() [config/LoggingInterceptor.java]           │
│                                                                                  │
│     a) Extract headers from HttpServletRequest:                                  │
│        correlationId = request.getHeader("X-Correlation-ID")  → "abc-123"        │
│        employeeId    = request.getHeader("X-Employee-ID")     → "emp-456"        │
│        (If missing: correlationId = UUID.randomUUID(), employeeId = "Unknown")    │
│                                                                                  │
│     b) Set MDC (thread-local logging context):                                   │
│        MDC.put("correlationId", "abc-123")                                       │
│        MDC.put("employeeId", "emp-456")                                          │
│        → ALL subsequent log statements on this thread auto-include these values  │
│                                                                                  │
│     c) Create OrderProcessingContext (Prototype bean — new instance per request): │
│        OrderProcessingContext ctx = contextProvider.getObject()                   │
│        ctx.setStartTime(System.currentTimeMillis())                              │
│        request.setAttribute("processingContext", ctx)                            │
│                                                                                  │
│     d) Log the incoming request:                                                 │
│        LOG: "Incoming request: POST /v1/orders"                                  │
│        Output: 2026-04-06 10:30:45 [http-nio-8081-exec-1] INFO                   │
│                [correlationId=abc-123] [employeeId=emp-456]                       │
│                Incoming request: POST /v1/orders                                 │
│                                                                                  │
│     e) return true → continue to controller                                      │
└──────────────────────────────────┬───────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│  4. JAKARTA VALIDATION (@Valid on @RequestBody)                                  │
│     Validates OrderRequest DTO fields:                                           │
│       ✓ productId: @NotNull                                                      │
│       ✓ name: @NotBlank, @Size(min=1, max=100)                                   │
│       ✓ price: @DecimalMin("0.0")                                                │
│       ✓ quantity: @Min(1)                                                        │
│     If INVALID → MethodArgumentNotValidException → GlobalExceptionHandler → 400  │
│     If VALID → proceed to controller method                                      │
└──────────────────────────────────┬───────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│  5. ORDER CONTROLLER — createOrder() [controller/OrderController.java]           │
│     @PostMapping("/v1/orders")                                                   │
│                                                                                  │
│     a) Call orderService.createOrder(orderRequest)                               │
│        → Delegates to the Singleton OrderService                                 │
└──────────────────────────────────┬───────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│  6. ORDER SERVICE — createOrder() [service/OrderService.java]                    │
│     @CacheEvict(value="orders", allEntries=true) ← clears cache on create       │
│                                                                                  │
│     a) LOG: "Creating order Laptop with productId=1"                             │
│                                                                                  │
│     b) SYNC REST CALL to Product Service (via RestTemplate):                     │
│        ┌────────────────────────────────────────────────────────────────────┐     │
│        │ getProductFromService(productId=1)                                 │     │
│        │ @CircuitBreaker(name="productService", fallback=...)               │     │
│        │ @Retry(name="productService") — up to 3 retries, 1s wait          │     │
│        │                                                                    │     │
│        │ GET http://localhost:8080/v1/products/1                            │     │
│        │      ↓                                                             │     │
│        │ ┌──────────────────────────────────────┐                           │     │
│        │ │ PRODUCT SERVICE (Port 8080)           │                           │     │
│        │ │ Returns: { id:1, name:"Laptop",       │                           │     │
│        │ │   price:1500.0, stock:10 }            │                           │     │
│        │ └──────────────────────────────────────┘                           │     │
│        │ If Product Service is DOWN:                                        │     │
│        │   → CircuitBreaker opens after 50% failures in 10 calls            │     │
│        │   → Fallback throws ServiceFallbackException → 503                 │     │
│        └────────────────────────────────────────────────────────────────────┘     │
│                                                                                  │
│     c) Create Order object:                                                      │
│        id=1, productId=1, name="Laptop", price=1500.0, quantity=2                │
│        status="PENDING"                                                          │
│        → Stored in in-memory HashMap                                             │
│                                                                                  │
│     d) LOG: "Order created with id=1, status=PENDING"                            │
│                                                                                  │
│     e) ASYNC KAFKA EVENT (via KafkaTemplate):                                    │
│        ┌────────────────────────────────────────────────────────────────────┐     │
│        │ sendOrderCreatedEvent(order, product)                              │     │
│        │ Topic: "order-created"                                             │     │
│        │ Key: "1" (orderId)                                                 │     │
│        │ Value: OrderEvent {                                                │     │
│        │   orderId: 1, productId: 1, quantity: 2,                           │     │
│        │   price: 1500.0, orderName: "Laptop", timestamp: 1712395845000     │     │
│        │ }                                                                  │     │
│        │ → Sent to Confluent Cloud Kafka (pkc-921jm.us-east-2.aws)          │     │
│        │ → Product Service @KafkaListener picks this up asynchronously      │     │
│        └────────────────────────────────────────────────────────────────────┘     │
│                                                                                  │
│     f) Map to OrderResponse and return                                           │
└──────────────────────────────────┬───────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│  7. BACK IN ORDER CONTROLLER                                                     │
│                                                                                  │
│     a) Optional: Record analytics via @Lazy OrderAnalyticsService                │
│        analytics = analyticsServiceProvider.getIfAvailable()                      │
│        If available → analytics.recordOrderCreated(1, "Laptop", 1500.0)          │
│                                                                                  │
│     b) Return ResponseEntity.ok(orderResponse)                                   │
│        → Spring serializes OrderResponse to JSON via Jackson                     │
└──────────────────────────────────┬───────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│  8. LOGGING INTERCEPTOR — afterCompletion() [config/LoggingInterceptor.java]     │
│                                                                                  │
│     a) Retrieve OrderProcessingContext from request attribute                    │
│     b) Calculate duration = currentTime - startTime                              │
│     c) LOG: "Request completed: Duration=45 ms, ResponseStatus=200"              │
│        Output: 2026-04-06 10:30:45 [http-nio-8081-exec-1] INFO                   │
│                [correlationId=abc-123] [employeeId=emp-456]                       │
│                Request completed: Duration=45 ms, ResponseStatus=200             │
│     d) MDC.clear() → clean up thread-local data (prevent leaks to next request)  │
│     e) OrderProcessingContext instance becomes eligible for garbage collection    │
└──────────────────────────────────┬───────────────────────────────────────────────┘
                                   │
                                   ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│  9. HTTP RESPONSE TO CLIENT                                                      │
│     HTTP/1.1 200 OK                                                              │
│     Content-Type: application/json                                               │
│     {                                                                            │
│       "id": 1, "productId": 1, "name": "Laptop",                                │
│       "description": "Gaming Laptop", "price": 1500.0, "status": "PENDING"       │
│     }                                                                            │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

### 16.2 Asynchronous Saga Flow (Happens AFTER Response)

The client already received a `200 OK` with status `PENDING`. Meanwhile, in the background:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  10. KAFKA BROKER (Confluent Cloud)                                             │
│      Topic: "order-created" receives the OrderEvent                             │
└──────────────────────────────────┬──────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  11. PRODUCT SERVICE — @KafkaListener (handleOrderCreated)                      │
│      Receives: OrderEvent { orderId:1, productId:1, quantity:2 }                │
│                                                                                 │
│      Check stock:                                                               │
│        product.stock=10, required=2 → 10 >= 2 ✅                                │
│        product.stock = 10 - 2 = 8                                               │
│                                                                                 │
│      Send: OrderSuccessEvent → topic "order-success"                            │
│      (OR: OrderFailedEvent  → topic "order-failed" if stock insufficient)       │
└──────────────────────────────────┬──────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  12. ORDER SERVICE — @KafkaListener (handleOrderSuccess/handleOrderFailed)       │
│      Receives: OrderSuccessEvent { orderId:1, status:"SUCCESS" }                │
│                                                                                 │
│      order.setStatus("CONFIRMED")  ← order status updated                      │
│      @CacheEvict clears cached order data                                       │
│                                                                                 │
│      LOG: "Order status updated to CONFIRMED: orderId=1"                        │
└─────────────────────────────────────────────────────────────────────────────────┘
```

**Final State:** `GET /v1/orders/1` now returns `"status": "CONFIRMED"`

---

### 16.3 Error Flow — What Happens When Things Fail

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│  ERROR SCENARIO 1: Validation Failure                                            │
│  Client sends: { "name": "", "quantity": 0 }                                    │
│                                                                                  │
│  Tomcat → DispatcherServlet → LoggingInterceptor.preHandle()                     │
│  → @Valid fails → MethodArgumentNotValidException                                │
│  → GlobalExceptionHandler.handleException()                                      │
│  → 400 Bad Request { "message": "...", "status": 400, "timestamp": ... }        │
│  → LoggingInterceptor.afterCompletion() (duration logged, MDC cleared)           │
└──────────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────────┐
│  ERROR SCENARIO 2: Product Service Down                                          │
│                                                                                  │
│  OrderService.getProductFromService(productId)                                   │
│  → RestTemplate.getForObject() throws ConnectException                           │
│  → @Retry: attempts 3 times with 1s wait between each                            │
│  → All 3 attempts fail                                                           │
│  → @CircuitBreaker: triggers fallback method                                     │
│  → productServiceFallback() throws ServiceFallbackException                      │
│  → GlobalExceptionHandler.handleServiceFallbackException()                       │
│  → 503 Service Unavailable                                                       │
│  → { "message": "[ProductService FALLBACK] Product Service is unavailable..." }  │
└──────────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────────┐
│  ERROR SCENARIO 3: Order Not Found                                               │
│  Client sends: GET /v1/orders/999                                                │
│                                                                                  │
│  OrderService.getOrderById(999)                                                  │
│  → orders.get(999) returns null                                                  │
│  → throws OrderNotFoundException("Order not found with id: 999")                 │
│  → GlobalExceptionHandler.handleOrderNotFoundException()                         │
│  → 404 Not Found { "message": "Order not found with id: 999", "status": 404 }   │
└──────────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────────┐
│  ERROR SCENARIO 4: Rate Limit Exceeded                                           │
│                                                                                  │
│  GET /v1/orders called more than 100 times per second                            │
│  → @RateLimiter(name="orderApi") triggers fallback                               │
│  → rateLimitFallback() throws ServiceFallbackException                           │
│  → 503 { "message": "[OrderService FALLBACK] Rate limit exceeded..." }           │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

### 16.4 Complete Log Trace for a Successful Order Creation

```log
2026-04-06 10:30:45.100 [http-nio-8081-exec-1] INFO  LoggingInterceptor
  [correlationId=abc-123] [employeeId=emp-456]
  Incoming request: POST /v1/orders

2026-04-06 10:30:45.102 [http-nio-8081-exec-1] INFO  OrderService
  [correlationId=abc-123] [employeeId=emp-456]
  Creating order Laptop with productId=1

2026-04-06 10:30:45.103 [http-nio-8081-exec-1] INFO  OrderService
  [correlationId=abc-123] [employeeId=emp-456]
  Calling Product Service for productId: 1

2026-04-06 10:30:45.150 [http-nio-8081-exec-1] INFO  OrderService
  [correlationId=abc-123] [employeeId=emp-456]
  Product found: id=1, price=1500.0, stock=10

2026-04-06 10:30:45.152 [http-nio-8081-exec-1] INFO  OrderService
  [correlationId=abc-123] [employeeId=emp-456]
  Order created with id=1, status=PENDING

2026-04-06 10:30:45.160 [http-nio-8081-exec-1] INFO  OrderService
  [correlationId=abc-123] [employeeId=emp-456]
  Kafka Event Sent: topic=order-created, orderId=1, productId=1, quantity=2

2026-04-06 10:30:45.162 [http-nio-8081-exec-1] INFO  LoggingInterceptor
  [correlationId=abc-123] [employeeId=emp-456]
  Request completed: Duration=62 ms, ResponseStatus=200

--- Async (after response sent) ---

2026-04-06 10:30:46.200 [kafka-listener-1] INFO  OrderService
  SUCCESS Event Received: orderId=1, status=SUCCESS

2026-04-06 10:30:46.201 [kafka-listener-1] INFO  OrderService
  Order status updated to CONFIRMED: orderId=1
```

---

### 16.5 Layer-by-Layer Summary

| # | Layer | Class | What Happens |
|---|-------|-------|--------------|
| 1 | **Server** | Tomcat | Accepts connection, assigns thread |
| 2 | **Servlet** | DispatcherServlet | Maps URL to controller method |
| 3 | **Interceptor** | LoggingInterceptor.preHandle() | MDC setup, timing start, log request |
| 4 | **Validation** | Jakarta @Valid | Validates DTO fields |
| 5 | **Controller** | OrderController | Routes to service, handles analytics |
| 6 | **Service** | OrderService | Business logic, REST call, Kafka publish |
| 7 | **External Call** | RestTemplate + Resilience4j | Sync call to Product Service with retry/circuit breaker |
| 8 | **Event Bus** | KafkaTemplate | Async event to Kafka topic |
| 9 | **Serialization** | Jackson | Java → JSON response |
| 10 | **Interceptor** | LoggingInterceptor.afterCompletion() | Duration calc, log response, MDC clear |
| 11 | **Exception** | GlobalExceptionHandler | Catches any exception, returns ErrorResponse JSON |

---

## 17. Swagger/OpenAPI Documentation

**SwaggerConfig.java:**
- Generates OpenAPI 3.0 documentation
- Defines reusable header components for `X-Correlation-Id` and `X-Employee-Id`
- Adds custom headers to all operations globally

**Access Swagger UI:**
```
http://localhost:8081/swagger-ui.html
```

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
@ConditionalOnProperty(name = "order.service.cache-enabled", havingValue = "true")
public String cacheEnabledMarker() { ... }

// @Qualifier: inject specific bean when multiple exist
@Autowired @Qualifier("consoleNotification")
private NotificationService notificationService;
```

---

## 19. Spring Boot DevTools

### What is DevTools?

**Spring Boot DevTools** (`spring-boot-devtools`) is a development-time tool that provides fast application restarts and live reload capabilities during development. It's already included in `pom.xml` with `<scope>runtime</scope>` and `<optional>true</optional>`, ensuring it's only used during development and excluded from production builds.

### Key Features

| Feature | Description | Benefit |
|---------|-------------|---------|
| **Fast Restart** | Detects class/resource changes and restarts the application | Faster feedback loop (1-2 seconds vs 10-15 seconds) |
| **Live Reload** | Automatically refreshes browser when resources change | No manual browser refresh needed |
| **Configuration Caching** | Caches configuration during restarts | Reduces restart time |
| **Class Loader Isolation** | Uses two separate class loaders | Restart only application classes, not dependencies |

### How It Works

```
File Change Detected (Java class, properties, template)
         ↓
Spring DevTools detects change
         ↓
Application restarts (using restart class loader)
         ↓
Browser auto-refresh (if Live Reload enabled)
         ↓
Changes visible immediately
```

### Automatic Restart Configuration

#### In IntelliJ IDEA (Recommended)

1. **Enable "Build Project Automatically":**
   - Go to: `Settings → Build, Execution, Deployment → Compiler`
   - ✅ Check "Build project automatically"

2. **Enable "Allow auto-make to start even if currently running":**
   - Go to: `Settings → Advanced Settings → Allow auto-make to start even if currently running`
   - ✅ Check this option

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **When you save a file**, IntelliJ auto-compiles it, DevTools detects the change and restarts the application (~2 seconds).

The restart is **much faster** than a full Maven build because DevTools uses a special restart class loader that only reloads your application code, not third-party libraries.

#### In Eclipse

1. Go to: `Preferences → General → Workspace`
2. ✅ Check "Refresh using native hooks or polling"
3. ✅ Check "Build automatically"

#### In VS Code

1. Open `.vscode/launch.json`
2. Add to run configuration:
```json
{
    "configurations": [
        {
            "name": "Spring Boot App",
            "type": "java",
            "preLaunchTask": "build",
            "mainClass": "com.example.order.order.OrderApplication",
            "projectName": "order",
            "cwd": "${workspaceFolder}",
            "args": "",
            "source": ["src/main/java"],
            "watch": ["src/main/java", "src/main/resources"]
        }
    ]
}
```

### Live Reload Browser Extension

For automatic browser refresh when resources change (CSS, HTML, templates):

1. **Install Live Reload browser extension:**
   - Chrome: [LiveJS Chrome Extension](https://chrome.google.com/webstore)
   - Firefox: [LiveJS Firefox Extension](https://addons.mozilla.org/firefox)

2. **Enable Live Reload in application.properties (optional):**
```properties
spring.devtools.remote.secret=mysecret
spring.devtools.livereload.enabled=true
```

3. **Activate the extension** in browser when running the app
4. Changes to templates, CSS, JavaScript automatically refresh the page

### Exclude Files from Restart Detection

Some files should NOT trigger restart (e.g., CSS, JS, HTML):

Add to `application.properties`:
```properties
spring.devtools.restart.exclude=static/**,public/**
spring.devtools.restart.additional-exclude=*.log
```

### Disable DevTools for Specific Operations

If you need to prevent automatic restart for some reason:

```properties
# Disable DevTools completely (still included but inactive)
spring.devtools.restart.enabled=false

# Disable Live Reload
spring.devtools.livereload.enabled=false
```

### Production Safety

DevTools is **automatically excluded** from production JAR files due to:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>       <!-- Only in development -->
    <optional>true</optional>    <!-- Not transitive dependency -->
</dependency>
```

Production JAR will be **smaller and faster** without DevTools.

### Practical Development Workflow with DevTools

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Start application: mvn spring-boot:run                   │
│    (Application running on http://localhost:8081)           │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Edit Java Class:                                         │
│    OrderController.java - Add new endpoint                  │
│    (Save file - Ctrl+S)                                     │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. IDE auto-compiles (if auto-build enabled)                │
│    ~ 100-200ms                                              │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. DevTools detects class file change                       │
│    Triggers restart (~1-2 seconds)                          │
│    ["Restarting application"]                               │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Test new endpoint in browser/curl:                       │
│    curl http://localhost:8081/v1/orders/new-endpoint       │
│    ✅ NEW endpoint available immediately                    │
└─────────────────────────────────────────────────────────────┘

Total time: ~2-3 seconds (vs 15+ seconds without DevTools)
```

### Common Scenarios & Solutions

**Issue: DevTools not detecting changes**
- Solution: Enable "Build automatically" in your IDE
- Verify: Check console for restart messages

**Issue: Restart takes too long (5+ seconds)**
- Solution: Reduce classpath (exclude unnecessary dependencies)
- Solution: Disable LiveReload if not needed

**Issue: LiveReload not working in browser**
- Solution: Install and activate LiveReload extension
- Solution: Check browser console for LiveReload connection

**Issue: Want to exclude large files from restart**
```properties
spring.devtools.restart.exclude=logs/**,*.log,cache/**
```

### Performance Tips

| Action | Impact |
|--------|--------|
| ✅ Edit Java classes + Save | Fast restart (~2s) |
| ✅ Edit application.properties | Fast restart (~2s) |
| ✅ Edit templates | Fast restart (~2s) |
| ❌ Add new dependencies | Full Maven build required |
| ❌ Change pom.xml | Full Maven build required |
| ❌ Add new library imports | May need full restart |

### DevTools vs Full Rebuild

| Method | Time | Use When |
|--------|------|----------|
| **DevTools Restart** | 2-3s | Editing Java classes, properties, templates (during development) |
| **mvn clean package** | 30-60s | Adding dependencies, releasing to production |
| **Full IDE Rebuild** | 10-15s | Major structural changes, compilation errors |

### Recommended Development Workflow

1. **Start application once:**
   ```bash
   mvn spring-boot:run
   ```

2. **Edit code in IDE** (maintain running application)

3. **Save file** (Ctrl+S / Cmd+S)

4. **DevTools auto-restarts** in 2-3 seconds

5. **Test immediately** in browser/curl

6. **Repeat** steps 2-5 without stopping the application

**Result:** Rapid development cycle with immediate feedback!

---

## 20. Spring Boot Actuator

### What is Actuator?

**Spring Boot Actuator** provides production-ready endpoints for monitoring and managing your application. It exposes metrics, health status, environment information, and more through HTTP endpoints.

### Key Benefits

| Benefit | Description |
|---------|-------------|
| **Health Monitoring** | Real-time application health status and dependency checks |
| **Metrics** | Collect and expose application metrics (requests, timing, memory) |
| **Environment Info** | View configuration properties and environment variables |
| **Debugging** | Thread dumps, heap dumps, loggers management |
| **Operations** | Monitor production behavior without code changes |

### Available Endpoints (Development vs Production)

| Endpoint | Purpose | Dev | Prod | Test |
|----------|---------|-----|------|------|
| `/actuator/health` | Application health status | ✅ | ✅ | ✅ |
| `/actuator/info` | Application info (name, version) | ✅ | ✅ | ✅ |
| `/actuator/metrics` | Application metrics (requests, memory, threads) | ✅ | ✅ | ✅ |
| `/actuator/env` | Environment variables and properties | ✅ | ❌ | ✅ |
| `/actuator/loggers` | View and modify logger levels | ✅ | ❌ | ✅ |
| `/actuator/threaddump` | Current thread dump (diagnostics) | ✅ | ❌ | ✅ |
| `/actuator/heapdump` | Download heap dump for memory analysis | ✅ | ❌ | ❌ |

### Configuration by Profile

#### Development Profile
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev
```

**All endpoints exposed** (security not a concern):
```
http://localhost:8081/actuator
http://localhost:8081/actuator/health
http://localhost:8081/actuator/metrics
http://localhost:8081/actuator/env
http://localhost:8081/actuator/threaddump
http://localhost:8081/actuator/heapdump
```

#### Production Profile
```bash
java -Dspring.profiles.active=prod -jar target/order-0.0.1-SNAPSHOT.jar
```

**Limited endpoints exposed** (security critical):
```
http://localhost:8080/actuator/health        ✅ Available
http://localhost:8080/actuator/info          ✅ Available
http://localhost:8080/actuator/metrics       ✅ Available
http://localhost:8080/actuator/env           ❌ Hidden (sensitive)
http://localhost:8080/actuator/loggers       ❌ Hidden
http://localhost:8080/actuator/threaddump    ❌ Hidden
```

### Usage Examples

#### 1. Health Check Endpoint

```bash
# Check application health
curl http://localhost:8081/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

**What it checks:**
- Application running
- Database connectivity
- Custom health indicators
- Dependency availability

#### 2. Metrics Endpoint

```bash
# List all available metrics
curl http://localhost:8081/actuator/metrics

# Get specific metric (e.g., JVM memory)
curl http://localhost:8081/actuator/metrics/jvm.memory.used

# Get HTTP request metrics
curl http://localhost:8081/actuator/metrics/http.server.requests
```

**Key Metrics:**
- `jvm.memory.used` - Current JVM memory usage
- `jvm.threads.live` - Active threads
- `http.server.requests` - HTTP request statistics
- `process.uptime` - Application uptime

#### 3. Environment Information

```bash
# View all environment properties (DEV ONLY)
curl http://localhost:8081/actuator/env

# View specific property group
curl http://localhost:8081/actuator/env/spring.application
```

**Returns:**
- Environment variables
- Application properties
- System properties
- Configuration sources

#### 4. Thread Dump (Diagnostics)

```bash
# Get current thread dump (useful for deadlock detection)
curl http://localhost:8081/actuator/threaddump
```

#### 5. Heap Dump (Memory Analysis)

```bash
# Download heap dump for memory leak analysis
curl http://localhost:8081/actuator/heapdump -o heap.hprof

# Analyze with tools like Eclipse MAT or jvisualvm
```

#### 6. Custom Application Info

Add to `application.properties`:
```properties
info.app.name=Order Service
info.app.description=Order Management Microservice
info.app.version=1.0.0
info.company.name=MyCompany
```

Then access:
```bash
curl http://localhost:8081/actuator/info
```

### Actuator Configuration Properties

#### Enable/Disable Endpoints

```properties
# Development - expose all
management.endpoints.web.exposure.include=*

# Production - expose only health, info, metrics
management.endpoints.web.exposure.include=health,info,metrics

# Exclude specific endpoints
management.endpoints.web.exposure.exclude=loggers,threaddump

# Base path for all actuator endpoints
management.endpoints.web.base-path=/actuator
```

#### Health Details Control

```properties
# Always show details (DEV)
management.endpoint.health.show-details=always

# Show only when authorized (PROD)
management.endpoint.health.show-details=when-authorized

# Never show details
management.endpoint.health.show-details=never
```

#### Environment Property Visibility

```properties
# Show all property values (DEV)
management.endpoint.env.show-values=always

# Never show values (PROD - sensitive data)
management.endpoint.env.show-values=never

# Show only when authorized
management.endpoint.env.show-values=when-authorized
```

### Real-world Monitoring Workflow

```
┌──────────────────────────────────────────────┐
│ Application Running on Production            │
│ (Port 8080 with PROD profile)                │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│ 1. Check Health Status                       │
│ curl /actuator/health                        │
│ Response: {"status":"UP"}                    │
│ ✅ All systems operational                   │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│ 2. Monitor Metrics                           │
│ curl /actuator/metrics                       │
│ - Request rate, response time                │
│ - Memory usage, GC activity                  │
│ - Thread count, pool status                  │
│ ⚠️  Notice: Memory increasing, GC triggered │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│ 3. Investigate Further (only in DEV)         │
│ Take thread dump: /actuator/threaddump       │
│ Download heap dump: /actuator/heapdump       │
│ Analyze memory leak with MAT                 │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│ 4. Alert/Action                              │
│ - Trigger log alerts                         │
│ - Scale horizontally                         │
│ - Restart container                          │
│ - Investigate code                           │
└──────────────────────────────────────────────┘
```

### Best Practices

**Development (application-dev.properties):**
```properties
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.endpoint.env.show-values=always
```
- ✅ All endpoints available for troubleshooting
- ✅ See detailed information
- ✅ Understand behavior

**Production (application-prod.properties):**
```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.endpoint.env.show-values=never
```
- ✅ Limited endpoints (security)
- ✅ No sensitive data exposure
- ✅ Only essential monitoring
- ✅ Require authentication for details

**Testing (application-test.properties):**
```properties
management.endpoints.web.exposure.include=health,info,metrics,env,threaddump,loggers
management.endpoint.health.show-details=always
management.endpoint.env.show-values=always
```
- ✅ Detailed info for test analysis
- ✅ Diagnostics available
- ✅ Environment visibility

### Securing Actuator in Production

For production deployments, consider:

1. **Authentication/Authorization:**
   ```properties
   # Require Spring Security for actuator access
   management.endpoints.web.exposure.include=health,info,metrics
   ```

2. **Different Port:**
   ```properties
   # Actuator on separate internal port
   management.server.port=9081
   management.server.address=127.0.0.1
   ```

3. **Network Restrictions:**
   - Use firewall rules to restrict actuator access
   - Only allow from monitoring system IP
   - Block from external networks

### Monitoring Tools Integration

Actuator endpoints can be integrated with:
- **Prometheus** - Metrics collection (`/actuator/prometheus`)
- **Grafana** - Visualization
- **ELK Stack** - Logging and analysis
- **New Relic** - APM monitoring
- **DataDog** - Observability platform
- **CloudWatch** - AWS monitoring

### Actuator in Distributed Systems

> **Note:** Actuator is already configured in this project. See the detailed Actuator section earlier in this README. Key points specific to our distributed setup:

#### Actuator in Distributed Systems

When running multiple microservices (Order + Product), Actuator helps monitor each independently:

```bash
# Order Service health
curl http://localhost:8081/actuator/health

# Product Service health
curl http://localhost:8080/actuator/health
```

#### Kafka Health Indicator

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

#### Useful Actuator Endpoints for Debugging Saga

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Check if service + Kafka are UP |
| `/actuator/metrics/kafka.consumer.records-consumed-total` | Total Kafka messages consumed |
| `/actuator/metrics/kafka.producer.record-send-total` | Total Kafka messages sent |
| `/actuator/metrics/http.server.requests` | REST API call metrics (RestTemplate calls) |
| `/actuator/loggers/com.example.order` | View/change log level at runtime |

#### Changing Log Level at Runtime (Without Restart)

```bash
# Check current log level
curl http://localhost:8081/actuator/loggers/com.example.order.order.service.OrderService

# Change to DEBUG
curl -X POST http://localhost:8081/actuator/loggers/com.example.order.order.service.OrderService \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# Change to WARN (reduce noise in prod)
curl -X POST http://localhost:8081/actuator/loggers/com.example.order.order.service.OrderService \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "WARN"}'
```

#### Actuator Configuration in Our Project

```properties
# application-dev.properties (all endpoints exposed)
management.endpoints.web.exposure.include=*
management.endpoints.web.base-path=/actuator
management.endpoint.health.show-details=always
management.endpoint.env.show-values=always

# application-prod.properties (restricted)
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

---

## 21. Distributed Saga Pattern with Kafka

#### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT REQUEST                              │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────────┐
│ STEP 1: Order Service - Sync Call to Product Service                │
│  ✓ Verify product exists                                            │
│  ✓ Get product price                                                │
│  ✓ Check if product is available                                    │
└──────────────┬───────────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────────┐
│ STEP 2: Create Order with PENDING Status                            │
│  ✓ Save order locally                                               │
│  ✓ Set status = "PENDING"                                           │
└──────────────┬───────────────────────────────────────────────────────┘
               │
               ▼
        ┌──────────────┐
        │    KAFKA      │
        └──────────────┘
               │
      ┌────────┼────────┐
      ▼                 ▼
STEP 3: Send         (Product Service subscribes)
order-created
Event
      │
      ▼
┌──────────────────────────────────────────────────────────────────────┐
│ STEP 4: Product Service - Listen to "order-created"                 │
│  ✓ Receive OrderEvent                                               │
│  ✓ Check stock availability                                         │
└──────────────┬─────────────────────────────────────────────────────┘
               │
       ┌───────┴───────┐
       │               │
    STOCK OK?       STOCK LOW?
       │               │
       ▼               ▼
  STEP 5a:        STEP 5b:
  Reduce Stock    Send Failed Event
  Send Success   (order-failed topic)
  Event
  (success topic)
```

#### Order Status Flow

```
CREATE → PENDING → CONFIRMED (if stock available)
                 → FAILED (if stock unavailable)
```

#### New Files Created for Kafka

| File | Description |
|------|-------------|
| `OrderEvent.java` | Event sent when order is created |
| `OrderSuccessEvent.java` | Event received when product stock check passes |
| `OrderFailedEvent.java` | Event received when product stock check fails |
| `ProductResponse.java` | Response DTO from Product Service |

#### Modified Files

- **pom.xml** — Added `spring-kafka` and `spring-boot-starter-validation` dependencies
- **OrderService.java** — Sync REST call + Kafka producer + Kafka listeners
- **AppConfig.java** — Added RestTemplate bean
- **application-dev.properties** — Kafka producer/consumer configuration

#### Implementation Flow

**STEP 1: Synchronous Product Service Call**
```java
ProductResponse product = getProductFromService(orderRequest);
// Calls Product Service at http://localhost:8080/v1/products/{productId}
```

**STEP 2: Save Order as PENDING**
```java
order.setStatus("PENDING");
orders.put(order.getId(), order);
```

**STEP 3: Send Kafka Event**
```java
kafkaTemplate.send("order-created", String.valueOf(order.getId()), orderEvent);
```

**Event Structure:**
```json
{
  "orderId": 1,
  "productId": 101,
  "quantity": 5,
  "price": 1500.00,
  "orderName": "Product Order",
  "timestamp": 1234567890
}
```

**STEP 4 & 5: Product Service Processes Order**
```java
@KafkaListener(topics = "order-created")
public void handleOrderCreated(OrderEvent event) {
    if (stock >= event.getQty()) {
        stock -= event.getQty();
        kafkaTemplate.send("order-success", successEvent);
    } else {
        kafkaTemplate.send("order-failed", failedEvent);
    }
}
```

**STEP 6a: Order Confirmed**
```java
@KafkaListener(topics = "order-success")
public void handleOrderSuccess(OrderSuccessEvent successEvent) {
    order.setStatus("CONFIRMED");
}
```

**STEP 6b: Order Failed**
```java
@KafkaListener(topics = "order-failed")
public void handleOrderFailed(OrderFailedEvent failedEvent) {
    order.setStatus("FAILED");
}
```

#### Kafka Topics

| Topic | Partition | Replication | Purpose |
|-------|-----------|-------------|---------|
| `order-created` | 1 | 1 | Order Service → Product Service |
| `order-success` | 1 | 1 | Product Service → Order Service |
| `order-failed` | 1 | 1 | Product Service → Order Service |

#### Kafka Configuration

**Local Kafka:**
```properties
spring.kafka.bootstrap-servers=localhost:9092
```

**Confluent Cloud:**
```properties
spring.kafka.bootstrap-servers=pkc-xyz123.us-east-1.provider.confluent.cloud:9092
spring.kafka.properties.security.protocol=SASL_SSL
spring.kafka.properties.sasl.mechanism=PLAIN
spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="YOUR_API_KEY" password="YOUR_API_SECRET";
```

**How to Get Confluent Cloud Credentials:**
1. Go to [Confluent Cloud Console](https://confluent.cloud)
2. Create a cluster
3. In **Cluster Settings** → Copy Bootstrap Servers
4. In **API Keys** → Create new API Key
5. Copy Key and Secret
6. Create Topics: `order-created`, `order-success`, `order-failed`

**Create Topics via CLI:**
```bash
confluent kafka topic create order-created --partitions 1 --replication-factor 1
confluent kafka topic create order-success --partitions 1 --replication-factor 1
confluent kafka topic create order-failed --partitions 1 --replication-factor 1
```

#### Testing the Saga Flow

**1. Start both services:**
```bash
# Order Service (port 8081)
mvn spring-boot:run

# Product Service (port 8080) - in product directory
mvn spring-boot:run
```

**2. Create an Order:**
```bash
curl -X POST http://localhost:8081/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Order",
    "description": "Gaming Laptop",
    "price": 101,
    "quantity": 2
  }'
```

**3. Check Order Status:**
```bash
curl http://localhost:8081/v1/orders/1
```

**Expected Logging Output:**
```
Creating order Laptop Order with productId=101
✓ Product found: id=101, price=1500.0, stock=10
✓ Order created with id=1, status=PENDING
✓ Kafka Event Sent: topic=order-created, orderId=1, productId=101, quantity=2
SUCCESS Event Received: orderId=1, status=CONFIRMED
✓ Order status updated to CONFIRMED: orderId=1
```

#### Error Scenarios

| Scenario | Result |
|----------|--------|
| Product Service unavailable | Order creation fails (503) |
| Product not found | Order creation fails (400) |
| Insufficient stock | Order status → FAILED |
| Kafka connection issues | Order created but event not sent |

---

**Architecture Pattern:** Distributed Saga (Choreography-based)
**Communication:** Synchronous (REST) + Asynchronous (Kafka)

Instead of Singleton:
```java
// ❌ DANGEROUS: Singleton storing per-request state
@Service
public class OrderService {
    private String currentUserId;    // SHARED across all requests! Race condition!
    private List<String> orderIds;  // SHARED across all requests!
}
```

Use Prototype + Manual Context Management:
```java
// ✅ SAFE: Prototype with per-request context
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderProcessingTask {
    private String userId;
    private List<String> orderIds;  // Isolated per task
    
    public void processOrders(String userId, List<String> orderIds) {
        this.userId = userId;        // Set per invocation
        this.orderIds = orderIds;
        // Process without race conditions
    }
}
```

#### How to Share Context Between Threads:

```java
// In Controller/Service
@Service
public class OrderService {
    @Autowired
    private ObjectProvider<OrderProcessingTask> taskProvider;
    
    public void processOrdersAsync(String userId, List<String> orderIds) {
        // Create prototype task instance
        OrderProcessingTask task = taskProvider.getObject();
        
        // Pass context via method parameter or setter
        task.setUserId(userId);
        task.setOrderIds(orderIds);
        
        // Submit to thread pool - each thread gets its own task instance
        executorService.submit(() -> {
            task.processOrders();  // No race condition!
        });
    }
}
```

**Best Practices for Multi-threaded Prototype Beans:**
- ✅ Use Prototype scope (new instance per request/task)
- ✅ Pass context explicitly via method parameters or setters
- ✅ DO NOT store request state in Singleton beans
- ✅ Use `ObjectProvider.getObject()` to get fresh instances
- ✅ Ensure thread isolation at data structure level (use concurrent collections if needed)
- ✅ Always clear resources after task completion

### Transactional Consistency Across Microservices

#### Problem: No Distributed Transactions

In a monolith, a single database transaction guarantees ACID properties. In microservices, each service has its own data store — **there is no global transaction**. If Order Service creates an order but Product Service fails to reduce stock, data becomes inconsistent.

```
❌ MONOLITH WAY (won't work in microservices):
@Transactional
public void placeOrder() {
    orderRepo.save(order);       // DB 1
    productRepo.reduceStock();   // DB 2 — DIFFERENT SERVICE!
}
```

#### Solution: Saga Pattern

A **Saga** is a sequence of local transactions where each service performs its own transaction and publishes an event. The next service listens to that event and performs its local transaction.

#### Two Types of Saga

| Type | How it works | Coordinator | Our Project |
|------|-------------|-------------|-------------|
| **Choreography** | Services listen to each other's events — no central controller | None (event-driven) | ✅ Yes |
| **Orchestration** | A central orchestrator tells each service what to do | Saga Orchestrator | ❌ No |

#### Our Choreography-Based Saga Flow

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

#### Why Choreography (Not Orchestration)?

| Aspect | Choreography (Ours) | Orchestration |
|--------|-------------------|---------------|
| **Complexity** | Simple for 2-3 services | Better for 5+ services |
| **Coupling** | Loose — services only know about events | Tighter — orchestrator knows all services |
| **Single Point of Failure** | None | Orchestrator is SPOF |
| **Debugging** | Harder to trace (distributed events) | Easier (centralized flow) |
| **Scalability** | Better — no bottleneck | Orchestrator can become bottleneck |

#### Transactional Guarantees

| Property | How We Achieve It |
|----------|------------------|
| **Atomicity** | Each service has its own local transaction (order save, stock reduce) |
| **Consistency** | Saga ensures eventual consistency via compensating events |
| **Isolation** | PENDING status prevents reading uncommitted state |
| **Durability** | Kafka persists events; services persist local data |

**Key Concept — Eventual Consistency:**
The system is **not immediately consistent** (order is PENDING until Product Service responds). But it **eventually becomes consistent** when the success/failure event arrives. This is acceptable in distributed systems.

---

## 22. Kafka Event Rollback - Compensating Transactions

#### What Happens When Something Fails?

In our saga, if Product Service finds insufficient stock, it doesn't "rollback" a database transaction — it sends a **compensating event** that tells Order Service to mark the order as FAILED.

#### Rollback Flow (Compensating Transaction)

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

#### How Rollback Works in Code

**Product Service (detects failure, sends compensating event):**
```java
@KafkaListener(topics = "order-created", groupId = "product-service-group")
public void handleOrderCreated(OrderEvent orderEvent) {
    Product product = products.get(orderEvent.getProductId());
    
    if (product == null) {
        // COMPENSATING EVENT: Product doesn't exist
        sendOrderFailedEvent(orderEvent, "Product not found");
        return;
    }
    
    if (product.getStock() < orderEvent.getQuantity()) {
        // COMPENSATING EVENT: Not enough stock
        sendOrderFailedEvent(orderEvent, "Insufficient stock");
        return;
    }
    
    // SUCCESS: Reduce stock and confirm
    product.setStock(product.getStock() - orderEvent.getQuantity());
    sendOrderSuccessEvent(orderEvent, product);
}
```

**Order Service (receives compensating event, "rolls back"):**
```java
@KafkaListener(topics = "order-failed", groupId = "order-service-group")
public void handleOrderFailed(OrderFailedEvent failedEvent) {
    Order order = orders.get(failedEvent.getOrderId());
    if (order != null) {
        order.setStatus("FAILED");  // Compensating action — mark as failed
    }
}
```

#### Key Differences: DB Rollback vs Saga Rollback

| Aspect | Database Rollback | Saga Compensating Transaction |
|--------|------------------|-------------------------------|
| **Mechanism** | `ROLLBACK` SQL command undoes changes | New event triggers reverse action |
| **Timing** | Immediate (synchronous) | Eventually (asynchronous) |
| **Scope** | Single database | Across multiple services |
| **Intermediate State** | None (all-or-nothing) | PENDING state visible |
| **Complexity** | Built-in to DB | Developer must implement |

#### Failure Scenarios & Handling

| Scenario | What Happens | Recovery |
|----------|-------------|----------|
| Product not found | `order-failed` event sent | Order → FAILED |
| Insufficient stock | `order-failed` event sent | Order → FAILED |
| Product Service down | No event sent | Order stays PENDING (needs timeout/retry) |
| Kafka down | Event not delivered | Retry with Kafka producer retries |
| Order Service misses event | Consumer re-reads from offset | `auto-offset-reset=earliest` |

#### Future Enhancement: Stock Rollback on Order Cancellation

If an order is cancelled **after** stock was reduced, you'd need another compensating event:

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

Confluent Cloud is a fully-managed, cloud-native Kafka platform that eliminates infrastructure complexity. This Order Service connects to Confluent Cloud for event-driven communication with Product Service through the Saga Pattern.

### Prerequisites

✅ Confluent Cloud Account (Free tier available at https://confluent.cloud)
✅ Both Order & Product services configured with KafkaConfig.java
✅ Spring Boot with spring-kafka dependency
✅ @EnableKafka annotation on Application class
✅ Java 17+ and Maven 3.6+

---

### Quick Start: 5 Easy Steps

#### STEP 1: Create Confluent Cloud Account
```
1. Visit https://confluent.cloud
2. Sign up with email or social account
3. Create Organization and verify email
4. Create Environment named "development"
5. Create Kafka Cluster (Standard, us-east-2 region)
   → Wait 5-10 minutes for cluster to be READY
```

**Status Check:**
```
Dashboard shows: Status: UP ✅
Bootstrap Server: pkc-921jm.us-east-2.aws.confluent.cloud:9092
```

#### STEP 2: Generate API Credentials
```
1. Cluster Dashboard → Cluster API Keys
2. Create new API key
   • Description: "Order Service Key"
   • Scope: Global Access
3. SAVE IMMEDIATELY (won't show again):
   • API Key: KOBXAXKO5FTSLBLB
   • API Secret: cfltI+IDXbYrU2...QKZAWSA
   • Bootstrap Server: pkc-921jm.us-east-2.aws.confluent.cloud:9092
```

#### STEP 3: Create Kafka Topics
```
Cluster Dashboard → Topics → Create Topic

Required Topics:
┌─────────────────────────────────────────────┐
│ Topic: order-created                        │
│ Partitions: 3                               │
│ Replication Factor: 3                       │
│ Retention: 7 days                           │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ Topic: order-success                        │
│ Partitions: 3                               │
│ Replication Factor: 3                       │
│ Retention: 7 days                           │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ Topic: order-failed                         │
│ Partitions: 3                               │
│ Replication Factor: 3                       │
│ Retention: 7 days                           │
└─────────────────────────────────────────────┘
```

#### STEP 4: Update application-dev.properties
```properties
# File: src/main/resources/application-dev.properties

spring.kafka.bootstrap-servers=pkc-921jm.us-east-2.aws.confluent.cloud:9092
spring.kafka.properties.security.protocol=SASL_SSL
spring.kafka.properties.sasl.mechanism=PLAIN
spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="YOUR_API_KEY" password="YOUR_API_SECRET";
```

#### STEP 5: Run Order Service
```bash
# Build
mvn clean install -DskipTests

# Run with dev profile (Confluent Cloud config)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

---

### Detailed Configuration

#### Complete application-dev.properties

```properties
spring.application.name=order-dev
server.port=8081
logging.level.root=DEBUG
logging.level.com.example.order=DEBUG
app.environment=development
app.api.timeout=5000

# ===================================================================
# CONFLUENT CLOUD KAFKA CONFIGURATION
# ===================================================================

# Bootstrap Server
spring.kafka.bootstrap-servers=pkc-921jm.us-east-2.aws.confluent.cloud:9092

# SASL/SSL Security (Required for Confluent Cloud)
spring.kafka.properties.security.protocol=SASL_SSL
spring.kafka.properties.sasl.mechanism=PLAIN
spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="KOBXAXKO5FTSLBLB" password="cfltI+IDXbYrU2mqj3wKwsTEpAfWQVXrPlZ9wPjKNKYjPtVkfPrZEJ538QKZAWSA";

# Producer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.properties.linger.ms=10

# Consumer Configuration
spring.kafka.consumer.group-id=order-service-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.max-poll-records=100

# Kafka Topics
kafka.topic.order-created=order-created
kafka.topic.order-success=order-success
kafka.topic.order-failed=order-failed
```

#### OrderApplication.java Configuration

```java
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka          // ✅ REQUIRED - enables Kafka listeners
@EnableAsync          // For @Async annotation support
@EnableCaching        // For @Cacheable annotation support
@EnableScheduling     // For @Scheduled annotation support
public class OrderApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderApplication.class);
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrderApplication.class, args);
        AppConfig appConfig = context.getBean(AppConfig.class);
        logger.info("Order Application started - Connected to Confluent Cloud");
        logger.info("Application configuration: {}", appConfig);
    }
}
```

#### KafkaConfig.java Setup

```java
package com.example.order.order.config;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    // ===== PRODUCER CONFIGURATION =====
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        
        // Confluent Cloud SASL_SSL is auto-configured from application-dev.properties
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    // ===== CONSUMER CONFIGURATION =====
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);  // 3 concurrent consumers
        return factory;
    }
}
```

---

### End-to-End Testing

#### Start Both Services

**Terminal 1: Product Service (port 8080)**
```bash
cd d:\Projects\Microservices_Latest\product
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Terminal 2: Order Service (port 8081)**
```bash
cd d:\Projects\Microservices_Latest\order
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### Complete Saga Flow Test

```bash
# Terminal 3: Create a Product
curl -X POST http://localhost:8080/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Mouse",
    "description": "Wireless gaming mouse",
    "price": 50.00,
    "quantity": 100
  }'
# Response: {"id": 1, "name": "Gaming Mouse", "stock": 100, ...}

# Terminal 3: Create Order for the Product
curl -X POST http://localhost:8081/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mouse Order",
    "description": "Order for gaming equipment",
    "price": 50.00,
    "productId": 1,
    "quantity": 5
  }'
# Response: {"id": 1, "productId": 1, "status": "PENDING", ...}

# Terminal 3: Check Status Immediately
curl http://localhost:8081/v1/orders/1
# Response: "status": "PENDING"

# Terminal 3: Wait for Kafka processing, then check again
sleep 3
curl http://localhost:8081/v1/orders/1
# Response: "status": "CONFIRMED" ✅ (Saga completed successfully!)
```

#### Kafka Event Timeline

```
T+0ms:   Order Service creates order (PENDING)
T+10ms:  Order Service publishes "order-created" to Kafka
T+50ms:  Product Service receives "order-created" event
T+60ms:  Product Service validates stock (100 >= 5 ✅)
T+70ms:  Product Service reduces stock (100 - 5 = 95)
T+80ms:  Product Service publishes "order-success" to Kafka
T+120ms: Order Service receives "order-success" event
T+130ms: Order Service updates order to CONFIRMED
```

---

### Verification Checklist

| Check | Action | Expected Result |
|-------|--------|-----------------|
| **Kafka Connection** | Check logs | `✅ CONNECTED TO CONFLUENT CLOUD` |
| **Topics Created** | View Confluent Console | 3 topics visible (order-created, order-success, order-failed) |
| **Messages Flowing** | Create order | Messages appear in Confluent Cloud console real-time |
| **Consumer Group** | Check Confluent Console | `order-service-group` shows members and lag |
| **Services Running** | curl endpoints | Both services respond (8080 & 8081) |

---

### Monitoring & Alerts

#### In Confluent Cloud Console

```
Cluster Dashboard → Topics → {topic-name}
Monitor:
├─ Messages in/out per second
├─ Consumer group lag
├─ Partition distribution
├─ Replication status
└─ Disk usage trends
```

#### Recommended Alerts

```
Set alerts for:
├─ Consumer lag > 10,000 messages
├─ Producer error rate > 1%
├─ Topic replication lag increasing
├─ Broker disk usage > 80%
└─ Network latency > 100ms
```

---

### Troubleshooting

| Issue | Error Message | Solution |
|-------|---------------|----------|
| **Cannot connect** | `timeout (ms) elapsed without establishing connection` | Verify bootstrap server, check firewall port 9092 |
| **Auth Failed** | `Authentication failed: Invalid username or password` | Create NEW API key, verify no typos in credentials |
| **Topic Missing** | `Topic 'order-created' doesn't exist` | Create topics manually in Confluent Cloud console |
| **SSL Error** | `SSLHandshakeException` | Ensure security.protocol=SASL_SSL is set |
| **No Messages** | Service runs but no events | Check KafkaConfig.java @Bean declarations |

---

### Production Best Practices

✅ **Security**
- Store credentials in AWS Secrets Manager (NOT in git)
- Use separate API keys per service/environment
- Rotate credentials every 90 days
- Enable audit logging

✅ **Reliability**
- Set up Dead Letter Queue (DLQ) for failed messages
- Implement circuit breaker for Kafka failures
- Add retry logic with exponential backoff
- Monitor consumer lag continuously

✅ **Performance**
- Enable batch processing (linger.ms, batch.size)
- Tune partition count based on throughput
- Set appropriate buffer sizes
- Monitor end-to-end latency

---

## 24. RestTemplate - Sync Communication

#### What is RestTemplate?

`RestTemplate` is a Spring HTTP client used for **synchronous** (blocking) REST API calls between microservices. In our project, Order Service uses RestTemplate to call Product Service before creating an order.

#### Why Synchronous Call First?

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

#### RestTemplate Configuration

```java
// AppConfig.java
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

> **Note:** Spring Boot 4.x with `spring-boot-starter-webmvc` does not include `RestTemplateBuilder`. We use `new RestTemplate()` directly.

#### RestTemplate Usage in Order Service

```java
private static final String PRODUCT_SERVICE_URL = "http://localhost:8080/v1/products/";

private ProductResponse getProductFromService(Long productId) {
    ProductResponse product = restTemplate.getForObject(
        PRODUCT_SERVICE_URL + productId,
        ProductResponse.class
    );
    if (product == null) {
        throw new ProductServiceException("Product not found with id: " + productId);
    }
    return product;
}
```

#### Common RestTemplate Methods

| Method | HTTP Verb | Use Case |
|--------|-----------|----------|
| `getForObject()` | GET | Fetch data (our use case) |
| `getForEntity()` | GET | Fetch data + headers + status |
| `postForObject()` | POST | Create resource, get response |
| `postForEntity()` | POST | Create resource, get full response |
| `put()` | PUT | Update resource |
| `delete()` | DELETE | Delete resource |
| `exchange()` | ANY | Full control (headers, method, body) |

#### RestTemplate vs WebClient vs OpenFeign

| Feature | RestTemplate | WebClient | OpenFeign |
|---------|-------------|-----------|-----------|
| **Blocking** | Yes (synchronous) | No (async/reactive) | Yes |
| **Spring version** | All versions | WebFlux (reactive) | Spring Cloud |
| **Ease of use** | Simple | Complex | Very simple |
| **Performance** | Blocks thread per call | Non-blocking I/O | Blocks thread |
| **Our choice** | ✅ Simple, sufficient | Overkill for 2 services | Needs Spring Cloud |

#### Error Handling with RestTemplate

In our project, RestTemplate errors are handled via `ProductServiceException`:

```java
try {
    ProductResponse product = restTemplate.getForObject(url, ProductResponse.class);
} catch (ProductServiceException e) {
    throw e;  // Re-throw known exceptions
} catch (Exception e) {
    throw new ProductServiceException("Product Service unavailable: " + e.getMessage());
}
```

The `GlobalExceptionHandler` then returns a structured error response:
```java
@ExceptionHandler(ProductServiceException.class)
public ResponseEntity<ErrorResponse> handleProductServiceException(ProductServiceException ex) {
    // Returns 503 SERVICE_UNAVAILABLE
}
```

---

## 25. Resilience4j - Fault Tolerance

All 5 Resilience4j patterns are implemented:

| Pattern | Where Used | Purpose |
|---------|-----------|---------|
| **CircuitBreaker** | `getProductFromService()` | Stops calling Product Service when it's down |
| **Retry** | `getProductFromService()` | Retries failed Product Service calls (3 attempts) |
| **RateLimiter** | `getAllOrders()` | Limits API calls to 100/sec |
| **Bulkhead** | `getOrderById()` | Max 20 concurrent calls |
| **TimeLimiter** | Configurable | Timeout for long operations |

**CircuitBreaker States:**
```
CLOSED (normal) → failures > 50% → OPEN (reject all)
                                      ↓ wait 10s
                                   HALF_OPEN (allow 3 test calls)
                                      ↓ success → CLOSED
                                      ↓ failure → OPEN
```

**Fallback with Custom Exception:**
```java
@CircuitBreaker(name = "productService", fallbackMethod = "productServiceFallback")
@Retry(name = "productService")
private ProductResponse getProductFromService(Long productId) { ... }

private ProductResponse productServiceFallback(Long productId, Throwable t) {
    throw new ServiceFallbackException("ProductService",
        "Product Service is unavailable for productId: " + productId);
}
```

**Configuration (`application-dev.properties`):**
```properties
resilience4j.circuitbreaker.instances.productService.sliding-window-size=10
resilience4j.circuitbreaker.instances.productService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.productService.wait-duration-in-open-state=10s
resilience4j.retry.instances.productService.max-attempts=3
resilience4j.ratelimiter.instances.orderApi.limit-for-period=100
resilience4j.bulkhead.instances.orderApi.max-concurrent-calls=20
```

---

## 26. @Async - Asynchronous Processing

```java
@Configuration
@EnableAsync
public class AsyncCacheSchedulingConfig {
    @Bean(name = "orderTaskExecutor")
    public Executor orderTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("order-async-");
        executor.initialize();
        return executor;
    }
}

// Usage in OrderService
@Async("orderTaskExecutor")
public CompletableFuture<List<OrderResponse>> getAllOrdersAsync(String name) { ... }
```

**Endpoint:** `GET /v1/orders/async`

---

## 27. @Cacheable - Spring Cache

```java
@EnableCaching  // in config class

@Cacheable(value = "orders", key = "'all-' + #name")
public List<OrderResponse> getAllOrders(String name) { ... }

@Cacheable(value = "orderById", key = "#id")
public OrderResponse getOrderById(Long id) { ... }

@CacheEvict(value = "orders", allEntries = true)
public OrderResponse createOrder(OrderRequest req) { ... }

@CacheEvict(value = {"orders", "orderById"}, allEntries = true)
public void handleOrderSuccess(OrderSuccessEvent event) { ... }
```

Cache is evicted on create and on Kafka status updates.

---

## 28. @Scheduled - Task Scheduling

```java
@EnableScheduling  // in config class

@Scheduled(fixedRateString = "${order.service.scheduling-rate-ms:60000}")
public void logOrderStats() {
    // Logs total, pending, confirmed, failed counts every 60s
}
```

---

## 29. API Versioning

```
/v1/orders      → Original API (full features)
/v2/orders      → New API (pagination by default)
```

```java
@RestController @RequestMapping("/v1/orders")
public class OrderController { ... }

@RestController @RequestMapping("/v2/orders")
class OrderControllerV2 {
    @GetMapping  // Returns PagedResponse by default
    public ResponseEntity<PagedResponse<OrderResponse>> getOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) { ... }
}
```

---

## 30. Pagination

```java
public PagedResponse<OrderResponse> getOrdersPaged(int page, int size) {
    // Returns subset of orders with total counts
}
```

**Request:** `GET /v1/orders/paged?page=0&size=5`

**Response:**
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 5,
  "totalElements": 23,
  "totalPages": 5
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
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
```

**Build & Run:**
```bash
docker build -t order-service .
docker run -p 8081:8081 order-service
```

---

## 32. Design Patterns Summary

1. **Singleton Pattern** — Stateless services, configs (default Spring scope)
2. **Prototype Pattern** — Per-request/per-task contexts with isolation
3. **Lazy Initialization Pattern** — @Lazy for expensive/optional bean initialization
4. **Interceptor Pattern** — LoggingInterceptor for cross-cutting concerns
5. **DTO Pattern** — OrderRequest, OrderResponse for data transfer
6. **Exception Handler Pattern** — @ControllerAdvice for centralized error handling
7. **Dependency Injection** — Constructor & field injection via Spring IoC
8. **MDC (Mapped Diagnostic Context)** — Thread-local logging context for request tracing
9. **TaskDecorator Pattern** — For propagating context across thread boundaries
10. **ObjectProvider Pattern** — For obtaining Prototype/Request-scoped/Lazy beans from Singletons

---

## 33. Future Enhancements

- [ ] Replace in-memory HashMap with database (JPA/Hibernate)
- [ ] Add Spring Security for authentication/authorization
- [ ] Add Redis for distributed caching
- [ ] Add Micrometer + Prometheus metrics
- [ ] Add docker-compose for multi-service orchestration

---

## Authors
Created as a learning microservice demonstrating Spring Boot best practices.
