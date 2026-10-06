package com.vidisha.InventoryService.controller;

import com.vidisha.InventoryService.entity.Inventory;
import com.vidisha.InventoryService.repository.InventoryRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:5173")
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public InventoryController(
            InventoryRepository inventoryRepository,
            RedisTemplate<String, Object> redisTemplate) {

        this.inventoryRepository = inventoryRepository;
        this.redisTemplate = redisTemplate;
    }

    // Add a new product
    @PostMapping
    public ResponseEntity<?> addProduct(
            @RequestBody Inventory inventory) {

        if (inventoryRepository
                .findByProduct(inventory.getProduct())
                .isPresent()) {

            return ResponseEntity
                    .badRequest()
                    .body("Product already exists");
        }

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        // Add newly created product to Redis
        String cacheKey =
                "inventory:" + savedInventory.getProduct();

        redisTemplate.opsForValue().set(
                cacheKey,
                savedInventory
        );

        return ResponseEntity.ok(savedInventory);
    }

    // Get all products
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryRepository.findAll()
        );
    }

    // Get inventory of a specific product
    @GetMapping("/{product}")
    public ResponseEntity<?> getInventory(
            @PathVariable String product) {

        String cacheKey = "inventory:" + product;

        // First check Redis
        Inventory inventory =
                (Inventory) redisTemplate
                        .opsForValue()
                        .get(cacheKey);

        if (inventory != null) {

            System.out.println(
                    "Inventory found in Redis cache."
            );

            return ResponseEntity.ok(inventory);
        }

        // Cache miss → check PostgreSQL
        inventory = inventoryRepository
                .findByProduct(product)
                .orElse(null);

        if (inventory == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        // Store PostgreSQL result in Redis
        redisTemplate.opsForValue().set(
                cacheKey,
                inventory
        );

        System.out.println(
                "Inventory loaded from PostgreSQL and stored in Redis."
        );

        return ResponseEntity.ok(inventory);
    }
}

