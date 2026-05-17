package com.ecommerce.order.services;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.client.ProductClient.ProductDto;
import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.entity.CartItem;
import com.ecommerce.order.repositories.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final ProductClient productClient;

    public CartService(CartRepository cartRepo, ProductClient productClient) {
        this.cartRepo = cartRepo;
        this.productClient = productClient;
    }

    @Transactional
    public Cart getCart(Long userId) {
        return cartRepo.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepo.save(cart);
        });
    }

    @Transactional
    public Cart addItem(Long userId, Integer productId, int quantity) {
        ProductDto product = productClient.getProduct(productId);
        if (product.stock() == null || product.stock() < quantity) {
            throw new IllegalStateException("Insufficient stock for product " + productId);
        }
        Cart cart = getCart(userId);
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUnitPrice(product.price());
        } else {
            CartItem item = new CartItem();
            item.setProductId(productId);
            item.setProductName(product.name());
            item.setUnitPrice(product.price());
            item.setQuantity(quantity);
            cart.getItems().add(item);
        }
        return cartRepo.save(cart);
    }

    @Transactional
    public Cart removeItem(Long userId, Integer productId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        return cartRepo.save(cart);
    }

    @Transactional
    public Cart clear(Long userId) {
        Cart cart = getCart(userId);
        cart.getItems().clear();
        return cartRepo.save(cart);
    }
}
