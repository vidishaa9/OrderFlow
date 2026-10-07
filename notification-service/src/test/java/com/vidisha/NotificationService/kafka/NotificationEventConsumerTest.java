package com.vidisha.NotificationService.kafka;

import com.vidisha.NotificationService.event.InventoryConfirmedEvent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationEventConsumerTest {

    @Test
    void shouldConsumeInventoryConfirmedEvent() {

        // Arrange
        NotificationEventConsumer consumer =
                new NotificationEventConsumer();

        InventoryConfirmedEvent event =
                new InventoryConfirmedEvent(
                        100L,
                        "Phone",
                        2
                );

        // Act & Assert
        assertDoesNotThrow(() ->
                consumer.consumeInventoryConfirmed(event)
        );
    }
}