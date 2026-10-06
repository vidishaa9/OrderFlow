package com.vidisha.InventoryService.service;

import com.vidisha.InventoryService.entity.Inventory;
import com.vidisha.InventoryService.repository.InventoryRepository;
import com.vidisha.InventoryService.repository.ProcessedOrderRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProcessedOrderRepository processedOrderRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldReduceStockAndMarkOrderAsProcessed() {

        // Arrange
        Inventory inventory = new Inventory();

        inventory.setId(1L);
        inventory.setProduct("Phone");
        inventory.setAvailableStock(10);

        when(processedOrderRepository.existsById(100L))
                .thenReturn(false);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("inventory:Phone"))
                .thenReturn(null);

        when(inventoryRepository.findByProduct("Phone"))
                .thenReturn(Optional.of(inventory));

        // Act
        Boolean result =
                inventoryService.checkAndReduceStock(
                        100L,
                        "Phone",
                        2
                );

        // Assert
        assertTrue(result);

        assertEquals(
                8,
                inventory.getAvailableStock()
        );

        verify(inventoryRepository, times(1))
                .save(inventory);

        verify(processedOrderRepository, times(1))
                .save(any());

        verify(valueOperations, times(2))
                .set("inventory:Phone", inventory);
    }

    @Test
    void shouldSkipDuplicateOrder() {

        // Arrange
        when(processedOrderRepository.existsById(100L))
                .thenReturn(true);

        // Act
        Boolean result =
                inventoryService.checkAndReduceStock(
                        100L,
                        "Phone",
                        2
                );

        // Assert
        assertNull(result);

        verify(processedOrderRepository, times(1))
                .existsById(100L);

        verify(inventoryRepository, never())
                .save(any());

        verify(processedOrderRepository, never())
                .save(any());

        verifyNoInteractions(redisTemplate);
    }
}