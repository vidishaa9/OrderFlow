package com.vidisha.OrderFlow.controller;

import com.vidisha.OrderFlow.dto.CreateOrderRequest;
import com.vidisha.OrderFlow.entity.Order;
import com.vidisha.OrderFlow.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(
                request.getProduct(),
                request.getQuantity()
        );

        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}