package com.vidisha.NotificationService.event;

public class InventoryConfirmedEvent {

    private Long orderId;
    private String product;
    private Integer quantity;

    public InventoryConfirmedEvent() {
    }

    public InventoryConfirmedEvent(
            Long orderId,
            String product,
            Integer quantity) {

        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}