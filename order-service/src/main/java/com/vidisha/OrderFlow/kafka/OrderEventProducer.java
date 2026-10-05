package com.vidisha.OrderFlow.kafka;

import com.vidisha.OrderFlow.event.OrderPlacedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {

        kafkaTemplate.send(
                "order-placed",
                event.getOrderId().toString(),
                event
        );
    }
}