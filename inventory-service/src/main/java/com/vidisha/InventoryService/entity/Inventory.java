package com.vidisha.InventoryService.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String product;

    @Column(nullable = false)
    private Integer availableStock;

    public Inventory() {
    }

    public Inventory(String product, Integer availableStock) {
        this.product = product;
        this.availableStock = availableStock;
    }

    public Long getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
}