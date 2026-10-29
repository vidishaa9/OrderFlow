package com.vidisha.InventoryService.repository;

import com.vidisha.InventoryService.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct(String product);

    // Atomic check + reduce in ONE SQL statement.
    // Returns 1 if stock was reduced, 0 if stock was not enough (or product not found).
    @Modifying
    @Query("UPDATE Inventory i " +
            "SET i.availableStock = i.availableStock - :qty " +
            "WHERE i.product = :product AND i.availableStock >= :qty")
    int reduceStockIfAvailable(@Param("product") String product,
                               @Param("qty") Integer qty);
}