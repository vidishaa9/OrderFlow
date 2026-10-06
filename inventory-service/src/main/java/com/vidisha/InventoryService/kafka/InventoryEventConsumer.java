package com.vidisha.InventoryService.kafka;

import com.vidisha.InventoryService.event.InventoryConfirmedEvent;
import com.vidisha.InventoryService.event.InventoryRejectedEvent;
import com.vidisha.InventoryService.event.OrderPlacedEvent;
import com.vidisha.InventoryService.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventConsumer {

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;

    public InventoryEventConsumer(
            InventoryService inventoryService,
            InventoryEventProducer inventoryEventProducer) {

        this.inventoryService = inventoryService;
        this.inventoryEventProducer = inventoryEventProducer;
    }

    @KafkaListener(
            topics = "order-placed",
            groupId = "inventory-service-group"
    )
    public void consumeOrderPlaced(OrderPlacedEvent event) {

        System.out.println(
                "========== INVENTORY EVENT RECEIVED =========="
        );

        System.out.println("Order ID: " + event.getOrderId());
        System.out.println("Product: " + event.getProduct());
        System.out.println("Quantity: " + event.getQuantity());

        Boolean stockResult =
                inventoryService.checkAndReduceStock(
                        event.getOrderId(),
                        event.getProduct(),
                        event.getQuantity()
                );

// Duplicate event
        if (stockResult == null) {

            System.out.println(
                    "Duplicate order event ignored."
            );

// Stock successfully reduced
        } else if (Boolean.TRUE.equals(stockResult)) {

            System.out.println(
                    "Stock available. Stock reduced."
            );

            InventoryConfirmedEvent confirmedEvent =
                    new InventoryConfirmedEvent(
                            event.getOrderId(),
                            event.getProduct(),
                            event.getQuantity()
                    );

            inventoryEventProducer.publishInventoryConfirmed(
                    confirmedEvent
            );

// Stock unavailable / product not found
        } else {

            System.out.println(
                    "Stock unavailable. Order cannot be fulfilled."
            );

            InventoryRejectedEvent rejectedEvent =
                    new InventoryRejectedEvent(
                            event.getOrderId(),
                            event.getProduct(),
                            event.getQuantity()
                    );

            inventoryEventProducer.publishInventoryRejected(
                    rejectedEvent
            );
        }
    }
}