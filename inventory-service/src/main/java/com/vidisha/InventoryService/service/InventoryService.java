package com.vidisha.InventoryService.service;

import com.vidisha.InventoryService.entity.Inventory;
import com.vidisha.InventoryService.entity.ProcessedOrder;
import com.vidisha.InventoryService.repository.InventoryRepository;
import com.vidisha.InventoryService.repository.ProcessedOrderRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

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

        // Step 1: Check if this order was already processed (idempotency)
        if (processedOrderRepository.existsById(orderId)) {

            System.out.println(
                    "Order " + orderId + " already processed. Skipping."
            );

            return null;
        }

        // Step 2: Atomic check + reduce directly in PostgreSQL
        // (single UPDATE ... WHERE availableStock >= quantity)
        int updatedRows =
                inventoryRepository.reduceStockIfAvailable(product, quantity);

        // Step 3: If no row was updated, stock was not enough (or product missing)
        if (updatedRows == 0) {

            System.out.println(
                    "Stock unavailable or product not found for: " + product
            );

            return false;
        }

        System.out.println(
                "Stock reduced in PostgreSQL for product: " + product
        );

        // Step 4: Mark order as processed (same transaction as the stock update)
        processedOrderRepository.save(
                new ProcessedOrder(orderId)
        );

        // Step 5: Remove the stale cache entry AFTER the transaction commits
        evictCacheAfterCommit("inventory:" + product);

        return true;
    }

    // Fast stock lookup: Redis first, PostgreSQL on a miss (cache-aside).
    // PostgreSQL remains the source of truth.
    public Integer getStock(String product) {

        String cacheKey = "inventory:" + product;

        try {
            Inventory cached =
                    (Inventory) redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                System.out.println("Inventory found in Redis cache.");
                return cached.getAvailableStock();
            }
        } catch (Exception e) {
            System.out.println(
                    "Redis unavailable, falling back to PostgreSQL."
            );
        }

        Inventory inventory =
                inventoryRepository.findByProduct(product).orElse(null);

        if (inventory == null) {
            return null;
        }

        try {
            // TTL: the cached value expires on its own after 10 minutes
            redisTemplate.opsForValue().set(
                    cacheKey,
                    inventory,
                    Duration.ofMinutes(10)
            );
            System.out.println(
                    "Inventory loaded from PostgreSQL and stored in Redis."
            );
        } catch (Exception e) {
            System.out.println("Could not write to Redis. Continuing.");
        }

        return inventory.getAvailableStock();
    }

    // Deletes the cache key only after the DB transaction has committed,
    // so Redis is never cleared for a change that was rolled back.
    private void evictCacheAfterCommit(String cacheKey) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            redisTemplate.delete(cacheKey);
                            System.out.println(
                                    "Redis cache evicted: " + cacheKey
                            );
                        } catch (Exception e) {
                            System.out.println(
                                    "Redis eviction failed (TTL will clean it up)."
                            );
                        }
                    }
                }
        );
    }
}