package com.vidisha.InventoryService.kafka;

import com.vidisha.InventoryService.event.InventoryConfirmedEvent;
import com.vidisha.InventoryService.event.InventoryRejectedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInventoryConfirmed(
            InventoryConfirmedEvent event) {

        kafkaTemplate.send(
                "inventory-confirmed",
                event.getOrderId().toString(),
                event
        );
    }

    public void publishInventoryRejected(
            InventoryRejectedEvent event) {

        kafkaTemplate.send(
                "inventory-rejected",
                event.getOrderId().toString(),
                event
        );
    }
}