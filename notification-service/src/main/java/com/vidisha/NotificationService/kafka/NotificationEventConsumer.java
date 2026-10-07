package com.vidisha.NotificationService.kafka;

import com.vidisha.NotificationService.event.InventoryConfirmedEvent;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventConsumer {

    @KafkaListener(
            topics = "inventory-confirmed",
            groupId = "notification-service-group"
    )
    public void consumeInventoryConfirmed(
            InventoryConfirmedEvent event) {

        System.out.println(
                "========== NOTIFICATION =========="
        );

        System.out.println(
                "Order confirmed successfully!"
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

        System.out.println(
                "Notification sent to customer."
        );

        System.out.println(
                "=================================="
        );
    }
}