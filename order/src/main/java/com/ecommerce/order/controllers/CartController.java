package com.ecommerce.order.controllers;

import com.ecommerce.order.dto.OrderDtos.AddItemRequest;
import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.services.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(@RequestHeader("X-User-Id") Long userId,
                                        @Valid @RequestBody AddItemRequest req) {
        return ResponseEntity.ok(cartService.addItem(userId, req.productId(), req.quantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Cart> removeItem(@RequestHeader("X-User-Id") Long userId,
                                           @PathVariable Integer productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @DeleteMapping
    public ResponseEntity<Cart> clear(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.clear(userId));
    }
}
