package com.vidisha.InventoryService.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "processed_orders")
public class ProcessedOrder {

    @Id
    private Long orderId;

    public ProcessedOrder() {
    }

    public ProcessedOrder(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}