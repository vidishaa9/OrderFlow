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
| Testing | JUnit, Mockito, Postman |

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
| **Inventory Service** | Consumes `order-placed`, reserves stock (Redis for fast reads, PostgreSQL as source of truth), and publishes `inventory-confirmed` or `inventory-rejected` |
| **Notification Service** | Consumes `order-placed` independently and sends (logs) a confirmation message |

---

## Order Flow

**Success**

1. Client calls `POST /api/orders`.
2. Order Service saves the order as `PENDING` and publishes `order-placed`.
3. Inventory Service checks and reserves stock, then publishes `inventory-confirmed`.
4. Order Service updates the order to `CONFIRMED`.
5. Notification Service, listening independently, sends the confirmation.

**Insufficient stock**

1. Inventory Service finds not enough stock and publishes `inventory-rejected`.
2. Order Service updates the order to `FAILED`.

An order is only marked `CONFIRMED` after inventory actually succeeds. No single transaction spans the services, so the status moves through events (a simplified saga-style flow).

---

## Kafka Topics and Events

| Topic | Published by | Consumed by |
|---|---|---|
| `order-placed` | Order Service | Inventory Service, Notification Service |
| `inventory-confirmed` | Inventory Service | Order Service |
| `inventory-rejected` | Inventory Service | Order Service |

Example `order-placed` event:

```json
{
  "orderId": 501,
  "productId": "mouse-01",
  "quantity": 2,
  "timestamp": "2026-09-13T10:00:00Z"
}
```

---

## Reliability Design

**Duplicate messages (idempotency).** Kafka delivers messages at least once, so the same event can arrive twice. The Inventory Service stores each processed `orderId` in a `processed_orders` table.

**Atomic stock update.** The stock deduction and the `processed_orders` insert happen inside one database transaction. Either both commit or both roll back. If the service crashes midway, nothing is committed, so a redelivered message is safely processed exactly once.

**Concurrent orders.** Stock is checked and reduced as a single atomic step, so when two orders compete for the last unit, one is confirmed and the other is rejected. No overselling.

**Service downtime.** If the Notification or Inventory Service is down, Kafka keeps the messages and the service catches up when it restarts.

**Redis vs PostgreSQL.** PostgreSQL is the source of truth. Redis is only a fast read cache for stock counts.

---

## Database Schema

**Order Service**

```
orders        (id, customer_id, status, created_at, updated_at)
order_items   (id, order_id, product_id, quantity)
```

**Inventory Service**

```
products          (id, name, stock_quantity)
processed_orders  (order_id, processed_at)
```

---

## Getting Started

### Prerequisites

- Java 17 or 21
- Maven
- Docker and Docker Compose

### Run everything

```bash
git clone https://github.com/<your-username>/orderflow.git
cd orderflow
docker-compose up --build
```

This starts Kafka, Redis, PostgreSQL, and all three services.

### Default ports

> Adjust these to match your `docker-compose.yml`.

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

A Postman collection is included in the `/postman` folder.

---

## Testing

```bash
mvn test
```

Key tests:

- **Duplicate message:** the same `order-placed` event is delivered twice, and stock decreases only once.
- **Insufficient stock:** the order ends as `FAILED`.
- **Concurrent orders:** several orders compete for the last unit, and exactly one succeeds.

---

## Try the Failure Scenarios

1. **Stop the Notification Service**, place an order, then start it again. The notification is still delivered.
2. **Place an order for more than the available stock.** The order ends as `FAILED`.
3. **Send the same event twice.** Stock is deducted once.

---

## Project Structure

```
orderflow/
├── order-service/
├── inventory-service/
├── notification-service/
├── postman/
├── docker-compose.yml
└── README.md
```

---

## Scope

Payments, real email delivery, and a frontend are intentionally out of scope. The project stays focused on event-driven communication and data consistency across services.


---

## Author

**Vidisha Arora** – [LinkedIn](https://www.linkedin.com/in/vidisha-arora-b39823290/) | [GitHub](https://github.com/vidishaa9)
