# OrderFlow – Event-Driven Order & Inventory System

OrderFlow is a microservices-based order processing system built with **Spring Boot** and **Apache Kafka**. Services communicate through events instead of calling each other directly, so placing an order never waits on stock updates or notifications.

It focuses on two real backend problems: keeping data consistent across services that each own their own database, and making sure stock is never oversold or deducted twice, even when messages are redelivered or orders arrive at the same time.

---

## Tech Stack

| Area | Technology |
|---|---|
| Language / Framework | Java, Spring Boot |
| Messaging | Apache Kafka |
| Cache | Redis |
| Database | PostgreSQL (one per service) |
| Containers | Docker, Docker Compose |
| Tools | Postman |

---

## Architecture

```
                 ┌─────────────────┐
                 │  Order Service  │
                 │  PostgreSQL     │
                 └────────┬────────┘
                          │  order-placed
                          ▼
                    ┌───────────┐
                    │   Kafka   │
                    └─────┬─────┘
                         / \
                        /   \
                       ▼     ▼
          ┌──────────────┐  ┌──────────────────┐
          │  Inventory   │  │   Notification   │
          │  Service     │  │   Service        │
          │  PostgreSQL  │  └──────────────────┘
          │  + Redis     │
          └──────┬───────┘
                 │  inventory-confirmed / inventory-rejected
                 ▼
          back to Order Service
          (updates order status)
```

| Service | Responsibility |
|---|---|
| **Order Service** | Accepts orders via REST, saves them as `PENDING`, publishes `order-placed`, and updates the order to `CONFIRMED` or `FAILED` based on inventory results |
| **Inventory Service** | Consumes `order-placed`, reduces stock atomically in PostgreSQL (the source of truth), uses Redis as a read cache, and publishes `inventory-confirmed` or `inventory-rejected` |
| **Notification Service** | Consumes `order-placed` independently and sends (logs) a confirmation message |

---

## Order Flow

**Success**

1. Client calls `POST /api/orders`.
2. Order Service saves the order as `PENDING` and publishes `order-placed`.
3. Inventory Service reduces stock and publishes `inventory-confirmed`.
4. Order Service updates the order to `CONFIRMED`.
5. Notification Service, listening independently, sends the confirmation.

**Insufficient stock**

1. Inventory Service finds not enough stock and publishes `inventory-rejected`.
2. Order Service updates the order to `FAILED`.

An order is only marked `CONFIRMED` after inventory actually succeeds. No single transaction spans the services, so the status moves through events (a simplified saga-style flow).

---

## Kafka Topics and Events

| Topic | Published by | Consumed by | Consumer group |
|---|---|---|---|
| `order-placed` | Order Service | Inventory Service, Notification Service | one group per service |
| `inventory-confirmed` | Inventory Service | Order Service | order service group |
| `inventory-rejected` | Inventory Service | Order Service | order service group |

Each service uses its own consumer group, so both Inventory and Notification receive every `order-placed` event.

Example `order-placed` event:

```json
{
  "orderId": 501,
  "product": "mouse-01",
  "quantity": 2
}
```

---

## Reliability Design

**Duplicate messages (idempotency).** Kafka delivers messages at least once, so the same event can arrive twice. The Inventory Service stores each processed `orderId` in a `processed_orders` table (primary key `orderId`) and skips events it has already handled. A duplicate is ignored and nothing is published again.

**Atomic stock update.** The stock deduction and the `processed_orders` insert happen inside one database transaction. Either both commit or both roll back, so after a crash a redelivered message is processed cleanly and stock is never deducted twice.

**Concurrent orders.** Stock is checked and reduced in a single conditional SQL statement (`UPDATE ... WHERE available_stock >= quantity`). The database serializes competing updates on the same row, so when two orders compete for the last unit, one is confirmed and the other is rejected. No overselling.

**Service downtime.** If the Notification or Inventory Service is down, Kafka keeps the messages and the service catches up when it restarts.

**Redis vs PostgreSQL.** PostgreSQL is the source of truth for every stock decision. Redis is a read cache using the cache-aside pattern with a 10-minute TTL. After a stock change commits, the cached entry is evicted, and reads fall back to PostgreSQL if Redis is unavailable.

### Known limitations and next improvements

- **Transactional outbox** for reliable event publishing (today the database commit and the Kafka publish are separate steps).
- **Dead-letter topic** with limited retries and backoff for failing messages.
- **Timeout handling** for orders that stay `PENDING` too long.
- **Idempotency in the Notification Service** to avoid duplicate notifications.
- **Flyway migrations** instead of `ddl-auto=update`, and secrets managed outside source code for production.

---

## Database Schema

**Order Service**

```
orders        (id, customer_id, status, created_at, updated_at)
order_items   (id, order_id, product_id, quantity)
```

**Inventory Service**

```
inventory         (id, product [unique], available_stock)
processed_orders  (order_id [primary key])
```

---

## Getting Started

### Prerequisites

- Java 17 or 21
- Maven
- Docker and Docker Compose

### Run everything

```bash
git clone https://github.com/vidishaa9/OrderFlow.git
cd OrderFlow
docker-compose up --build
```

This starts Kafka, Redis, PostgreSQL, and all services.

### Default ports

| Service | Port |
|---|---|
| Order Service | 8081 |
| Inventory Service | 8082 |
| Notification Service | 8083 |

---

## API

### Place an order

```http
POST /api/orders
Content-Type: application/json

{
  "customerId": "priya123",
  "items": [
    { "productId": "mouse-01", "quantity": 2 }
  ]
}
```

Response: the created order with status `PENDING`.

### Get order status

```http
GET /api/orders/{id}
```

Returns the order with its current status: `PENDING`, `CONFIRMED`, or `FAILED`.

### Check stock (Inventory Service)

```http
GET /api/inventory/{product}
```

Returns the available stock for a product. Served from Redis when cached, otherwise loaded from PostgreSQL.

---

## Test Scenarios

Try these with Postman or `curl`:

1. **Normal order:** place an order for available stock. The status moves from `PENDING` to `CONFIRMED`, and the stock decreases.
2. **Insufficient stock:** order more than is available. The order ends as `FAILED`, and the stock does not change.
3. **Duplicate event:** deliver the same `order-placed` event (same `orderId`) twice. The second one is logged as already processed, and stock is reduced only once.
4. **Last unit:** set stock to 1 and place two orders at the same time. Exactly one is confirmed, one fails, and stock ends at 0 (never negative).
5. **Service downtime:** stop the Notification Service, place an order, then start it again. The notification is still delivered.
6. **Redis cache:** call `GET /api/inventory/{product}` twice. The first call loads from PostgreSQL and the second is served from Redis. In `redis-cli`, `TTL inventory:<product>` shows the remaining expiry.

---

## Project Structure

```
OrderFlow/
├── order-service/
├── inventory-service/
├── notification-service/
├── frontend/
├── docker-compose.yml
└── README.md
```

---

## Scope

Payments and real email delivery are intentionally out of scope. The project stays focused on event-driven communication and data consistency across services.

---

## Author

**Vidisha Arora** – [LinkedIn](https://www.linkedin.com/in/vidisha-arora-b39823290/) | [GitHub](https://github.com/vidishaa9)
