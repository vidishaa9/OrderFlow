package com.vidisha.OrderFlow.kafka;

import com.vidisha.OrderFlow.entity.Order;
import com.vidisha.OrderFlow.event.InventoryConfirmedEvent;
import com.vidisha.OrderFlow.event.InventoryRejectedEvent;
import com.vidisha.OrderFlow.repository.OrderRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private final OrderRepository orderRepository;

    public OrderEventConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    // ==================== INVENTORY CONFIRMED ====================

    @KafkaListener(
            topics = "inventory-confirmed",
            groupId = "order-service-confirmed-group",
            containerFactory = "confirmedKafkaListenerContainerFactory"
    )
    public void consumeInventoryConfirmed(
            InventoryConfirmedEvent event) {

        System.out.println(
                "========== INVENTORY CONFIRMED =========="
        );

        System.out.println(
                "Order ID: " + event.getOrderId()
        );

        System.out.println(
                "Product: " + event.getProduct()
        );

        System.out.println(
                "Quantity: " + event.getQuantity()
        );


        Order order = orderRepository
                .findById(event.getOrderId())
                .orElse(null);


        if (order != null) {

            order.setStatus("CONFIRMED");

            orderRepository.save(order);

            System.out.println(
                    "Order status updated to CONFIRMED."
            );

        } else {

            System.out.println(
                    "Order not found: " + event.getOrderId()
            );
        }


        System.out.println(
                "=========================================="
        );
    }


    // ==================== INVENTORY REJECTED ====================

    @KafkaListener(
            topics = "inventory-rejected",
            groupId = "order-service-rejected-group",
            containerFactory = "rejectedKafkaListenerContainerFactory"
    )
    public void consumeInventoryRejected(
            InventoryRejectedEvent event) {

        System.out.println(
                "========== INVENTORY REJECTED =========="
        );

        System.out.println(
                "Order ID: " + event.getOrderId()
        );

        System.out.println(
                "Product: " + event.getProduct()
        );

        System.out.println(
                "Quantity: " + event.getQuantity()
        );


        Order order = orderRepository
                .findById(event.getOrderId())
                .orElse(null);


        if (order != null) {

            order.setStatus("FAILED");

            orderRepository.save(order);

            System.out.println(
                    "Order status updated to FAILED."
            );

        } else {

            System.out.println(
                    "Order not found: " + event.getOrderId()
            );
        }


        System.out.println(
                "========================================"
        );
    }
}