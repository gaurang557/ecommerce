package com.ecommerce.order.controllers;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.checkout(userId));
    }

    @GetMapping
    public ResponseEntity<List<Order>> list(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.listOrders(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@RequestHeader("X-User-Id") Long userId,
                                     @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(userId, id));
    }
}
