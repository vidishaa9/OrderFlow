package com.vidisha.OrderFlow.repository;

import com.vidisha.OrderFlow.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}