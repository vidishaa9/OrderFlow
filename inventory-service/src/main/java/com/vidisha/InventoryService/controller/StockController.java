package com.vidisha.InventoryService.controller;

import com.vidisha.InventoryService.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class StockController {

    private final InventoryService inventoryService;

    public StockController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{product}")
    public ResponseEntity<Integer> getStock(@PathVariable String product) {
        Integer stock = inventoryService.getStock(product);
        return stock == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(stock);
    }
}