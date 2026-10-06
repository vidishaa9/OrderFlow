package com.vidisha.InventoryService.repository;

import com.vidisha.InventoryService.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct(String product);
}