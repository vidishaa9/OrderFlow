package com.vidisha.InventoryService.service;

import com.vidisha.InventoryService.entity.Inventory;
import com.vidisha.InventoryService.entity.ProcessedOrder;
import com.vidisha.InventoryService.repository.InventoryRepository;
import com.vidisha.InventoryService.repository.ProcessedOrderRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProcessedOrderRepository processedOrderRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProcessedOrderRepository processedOrderRepository,
            RedisTemplate<String, Object> redisTemplate) {

        this.inventoryRepository = inventoryRepository;
        this.processedOrderRepository = processedOrderRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Boolean checkAndReduceStock(
            Long orderId,
            String product,
            Integer quantity) {

        // Step 1: Check if this order was already processed
        if (processedOrderRepository.existsById(orderId)) {

            System.out.println(
                    "Order " + orderId + " already processed. Skipping."
            );

            return null;
        }

        // Step 2: Check Redis cache first
        String cacheKey = "inventory:" + product;

        Inventory inventory =
                (Inventory) redisTemplate.opsForValue().get(cacheKey);

        if (inventory != null) {

            System.out.println(
                    "Inventory found in Redis cache."
            );

        } else {

            System.out.println(
                    "Inventory not found in Redis. Checking PostgreSQL."
            );

            inventory = inventoryRepository
                    .findByProduct(product)
                    .orElse(null);

            if (inventory == null) {
                return false;
            }

            // Store database result in Redis
            redisTemplate.opsForValue().set(
                    cacheKey,
                    inventory
            );

            System.out.println(
                    "Inventory loaded from PostgreSQL and stored in Redis."
            );
        }

        // Step 3: Check available stock
        if (inventory.getAvailableStock() < quantity) {

            return false;
        }

        // Step 4: Reduce stock
        inventory.setAvailableStock(
                inventory.getAvailableStock() - quantity
        );

        // Save updated stock to PostgreSQL
        inventoryRepository.save(inventory);

        // Step 5: Update Redis cache
        redisTemplate.opsForValue().set(
                cacheKey,
                inventory
        );

        System.out.println(
                "Redis cache updated for product: " + product
        );

        // Step 6: Mark order as processed
        processedOrderRepository.save(
                new ProcessedOrder(orderId)
        );

        return true;
    }
}

