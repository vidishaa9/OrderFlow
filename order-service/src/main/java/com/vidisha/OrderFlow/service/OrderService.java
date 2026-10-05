package com.vidisha.OrderFlow.service;

import com.vidisha.OrderFlow.entity.Order;
import com.vidisha.OrderFlow.event.OrderPlacedEvent;
import com.vidisha.OrderFlow.kafka.OrderEventProducer;
import com.vidisha.OrderFlow.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(
            OrderRepository orderRepository,
            OrderEventProducer orderEventProducer) {

        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    public Order createOrder(String product, Integer quantity) {

        // 1. Create the order
        Order order = new Order();

        order.setProduct(product);
        order.setQuantity(quantity);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        // 2. Save order in PostgreSQL
        Order savedOrder = orderRepository.save(order);

        // 3. Create an event
        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getProduct(),
                savedOrder.getQuantity()
        );

        // 4. Publish event to Kafka
        orderEventProducer.publishOrderPlaced(event);

        // 5. Return the saved order
        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}