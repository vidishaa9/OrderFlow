package com.vidisha.OrderFlow.service;

import com.vidisha.OrderFlow.entity.Order;
import com.vidisha.OrderFlow.event.OrderPlacedEvent;
import com.vidisha.OrderFlow.kafka.OrderEventProducer;
import com.vidisha.OrderFlow.repository.OrderRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderAndPublishEvent() {

        // Arrange
        Order savedOrder = new Order();

        savedOrder.setId(1L);
        savedOrder.setProduct("Phone");
        savedOrder.setQuantity(2);
        savedOrder.setStatus("PENDING");

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder("Phone", 2);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Phone", result.getProduct());
        assertEquals(2, result.getQuantity());
        assertEquals("PENDING", result.getStatus());

        // Verify order was saved
        verify(orderRepository, times(1))
                .save(any(Order.class));

        // Capture the Kafka event
        ArgumentCaptor<OrderPlacedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderPlacedEvent.class);

        verify(orderEventProducer, times(1))
                .publishOrderPlaced(eventCaptor.capture());

        OrderPlacedEvent event = eventCaptor.getValue();

        assertEquals(1L, event.getOrderId());
        assertEquals("Phone", event.getProduct());
        assertEquals(2, event.getQuantity());
    }
}

