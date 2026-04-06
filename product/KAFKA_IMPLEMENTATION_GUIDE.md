# Product Service - Distributed Saga Implementation 🎯

## What's Been Updated

The Product Service is now fully equipped to:
1. **STEP 4️⃣**: Listen to `order-created` events from Order Service
2. **STEP 5️⃣**: Validate product stock availability
3. Send `order-success` or `order-failed` events back to Order Service

## Architecture Flow

```
Order Service (creates order)
        │
        ├─→ STEP 3: Sends "order-created" event to Kafka
        │
        ▼
┌──────────────────────────┐
│   KAFKA: order-created   │
└──────────────┬───────────┘
               │
               ▼
        Product Service
        │
        ├─→ STEP 4: @KafkaListener listens
        │
        ├─→ Check product exists
        ├─→ Check stock availability
        │
        ├─→ IF STOCK OK:
        │   ├─ Reduce stock
        │   └─ Send "order-success" event
        │
        └─→ IF STOCK LOW:
            └─ Send "order-failed" event
        │
        ▼
┌──────────────────────────┐
│ KAFKA: order-success or  │
│        order-failed      │
└──────────────┬───────────┘
               │
               ▼
        Order Service
        (receives & updates)
```

## Modified Files in Product Service

### 1. [pom.xml](pom.xml)
- ✅ Added `spring-kafka` dependency
- ✅ Added `spring-boot-starter-validation` dependency

### 2. [Product.java](src/main/java/com/example/product/product/model/Product.java)
```java
private Integer stock;  // NEW: Track available stock
```

### 3. [ProductResponse.java](src/main/java/com/example/product/product/dto/ProductResponse.java)
```java
private final Integer stock;    // NEW: Include stock in response
private final String status;    // NEW: Product status
```

### 4. [ProductService.java](src/main/java/com/example/product/product/service/ProductService.java)

**New Kafka Listener:**
```java
@KafkaListener(topics = "order-created", groupId = "product-service-group")
public void handleOrderCreated(OrderEvent orderEvent) {
    // STEP 4: Receive order event
    Product product = products.get(orderEvent.getProductId());
    
    // Check stock
    if (product.getStock() >= orderEvent.getQuantity()) {
        product.setStock(product.getStock() - orderEvent.getQuantity());
        sendOrderSuccessEvent(orderEvent, product);  // STEP 5a
    } else {
        sendOrderFailedEvent(orderEvent);  // STEP 5b
    }
}
```

### 5. [application.properties](src/main/resources/application.properties)
- ✅ Added Kafka producer/consumer configuration
- ✅ Added Confluent Cloud template (commented out)

## New Event DTOs Created

1. **[OrderEvent.java](src/main/java/com/example/product/product/dto/OrderEvent.java)**
   - Received from Order Service

2. **[OrderSuccessEvent.java](src/main/java/com/example/product/product/dto/OrderSuccessEvent.java)**
   - Sent when stock is confirmed

3. **[OrderFailedEvent.java](src/main/java/com/example/product/product/dto/OrderFailedEvent.java)**
   - Sent when stock is insufficient

## Testing the Integration

### 1. Start Product Service (default port 8080)
```bash
mvn spring-boot:run
```

### 2. Create a Product First

**Request:**
```bash
curl -X POST http://localhost:8080/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "Gaming Laptop Pro",
    "price": 1500.00,
    "quantity": 10
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "Gaming Laptop Pro",
  "price": 1500.0,
  "stock": 10,
  "status": "AVAILABLE"
}
```

**Note:** `productId=1` for the order creation step

### 3. Start Order Service (port 8081)
```bash
# In order service directory
mvn spring-boot:run
```

### 4. Create an Order

**Request:**
```bash
curl -X POST http://localhost:8081/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Order",
    "description": "First order",
    "price": 1,
    "quantity": 2
  }'
```

**Response (Immediate):**
```json
{
  "id": 1,
  "name": "My Order",
  "description": "First order",
  "price": 1500.0,
  "status": "PENDING"
}
```

### 5. Wait 2-3 Seconds, Then Check Order Status

**Request:**
```bash
curl http://localhost:8081/v1/orders/1
```

**Response (After Kafka Event Processing):**

**✅ SUCCESS Case (Stock Available):**
```json
{
  "id": 1,
  "name": "My Order",
  "description": "First order",
  "price": 1500.0,
  "status": "CONFIRMED"  // Status updated by Product Service confirmation
}
```

**❌ FAILED Case (Stock Unavailable):**
```json
{
  "id": 1,
  "name": "My Order",
  "description": "First order",
  "price": 1500.0,
  "status": "FAILED"     // Status updated due to insufficient stock
}
```

### 6. Verify Product Stock Was Reduced

**Request:**
```bash
curl http://localhost:8080/v1/products/1
```

**Response (After Successful Order):**
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "Gaming Laptop Pro",
  "price": 1500.0,
  "stock": 8,      // Reduced from 10 to 8 (ordered 2)
  "status": "AVAILABLE"
}
```

## Kafka Log Output

When processing an order, you should see:

### Order Service Logs:
```
Creating order My Order with productId=1
✓ Product found: id=1, price=1500.0, stock=10
✓ Order created with id=1, status=PENDING
✓ Kafka Event Sent: topic=order-created, orderId=1, productId=1, quantity=2
📨 SUCCESS Event Received: orderId=1, status=SUCCESS
✓ Order status updated to CONFIRMED: orderId=1
```

### Product Service Logs:
```
📨 Received order-created event: orderId=1, productId=1, quantity=2
✓ Product found: id=1, name=Laptop, stock=10, required_qty=2
✓ Stock reduced: productId=1, stock_before=10, stock_after=8
✓ Kafka Event Sent: topic=order-success, orderId=1, status=SUCCESS
```

## Confluent Cloud Configuration

To use Confluent Cloud instead of local Kafka:

1. **Get Credentials from Confluent Cloud Console:**
   - Bootstrap Servers (e.g., `pkc-xyz.us-east-1.provider.confluent.cloud:9092`)
   - API Key and Secret

2. **Update `application.properties`:**

```properties
spring.kafka.bootstrap-servers=pkc-xyz.us-east-1.provider.confluent.cloud:9092

spring.kafka.properties.security.protocol=SASL_SSL
spring.kafka.properties.sasl.mechanism=PLAIN
spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="API_KEY" password="API_SECRET";

spring.kafka.properties.schema.registry.url=https://psrc-xyz.us-east-1.provider.confluent.cloud
spring.kafka.properties.schema.registry.basic.auth.credentials.source=USER_INFO
spring.kafka.properties.schema.registry.basic.auth.user.info=SR_KEY:SR_SECRET
```

## Error Scenarios

### ❌ Product Not Found
```
Product Service receives orderId=5, productId=999
→ Logs: ✗ Product not found for productId: 999
→ Sends: "order-failed" event with reason "Product not found"
→ Order Status: FAILED
```

### ❌ Insufficient Stock
```
Product Service receives orderId=2, quantity=15 (but stock=10)
→ Logs: ✗ Insufficient stock: required=15, available=10
→ Sends: "order-failed" event with reason "Insufficient stock"
→ Order Status: FAILED
→ Stock: Unchanged (not reduced)
```

### ✅ Sufficient Stock
```
Product Service receives orderId=1, quantity=2 (stock=10)
→ Stock: Reduced to 8
→ Sends: "order-success" event
→ Order Status: CONFIRMED
```

## Kafka Topics Used

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `order-created` | Order Service | Product Service | Order creation request |
| `order-success` | Product Service | Order Service | Stock confirmed event |
| `order-failed` | Product Service | Order Service | Stock unavailable event |

## Complete Distributed Saga Pattern

This implementation demonstrates the **Choreography-based Saga Pattern**:

1. **STEP 1️⃣-3️⃣**: Order Service initiates
2. **STEP 4️⃣-5️⃣**: Product Service responds
3. **STEP 6️⃣**: Order Service finalizes

**Key Benefits:**
- ✅ Decoupled services (no direct calls)
- ✅ Event-driven architecture
- ✅ Asynchronous processing
- ✅ Better scalability
- ✅ Failure handling with compensation

## Next Steps

- Monitor logs to ensure Kafka events are flowing correctly
- Test both success and failure scenarios
- Implement retry logic if needed
- Add database persistence instead of in-memory storage
- Implement deadletter queues for failed events

---

**Pattern:** Distributed Saga (Choreography-based)  
**Status:** ✅ Ready for Production Testing
