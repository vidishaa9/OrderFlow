package com.vidisha.InventoryService.event;

public class OrderPlacedEvent {

    private Long orderId;
    private String product;
    private Integer quantity;

    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(Long orderId, String product, Integer quantity) {
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}