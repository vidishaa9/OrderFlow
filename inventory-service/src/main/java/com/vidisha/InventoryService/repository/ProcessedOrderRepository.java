package com.vidisha.InventoryService.repository;

import com.vidisha.InventoryService.entity.ProcessedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderRepository
        extends JpaRepository<ProcessedOrder, Long> {
}