package com.ecommerce.order.services;

import com.ecommerce.order.client.PaymentClient;
import com.ecommerce.order.client.PaymentClient.ChargeResult;
import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.repositories.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final CartService cartService;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;

    public OrderService(OrderRepository orderRepo, CartService cartService,
                        ProductClient productClient, PaymentClient paymentClient) {
        this.orderRepo = orderRepo;
        this.cartService = cartService;
        this.productClient = productClient;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public Order checkout(Long userId) {
        Cart cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (var ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setProductId(ci.getProductId());
            oi.setProductName(ci.getProductName());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setQuantity(ci.getQuantity());
            order.getItems().add(oi);
            total = total.add(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setTotal(total);
        order = orderRepo.save(order);

        ChargeResult result = paymentClient.charge(order.getId(), userId, total);
        if (result == null || !"SUCCESS".equals(result.status())) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepo.save(order);
            throw new IllegalStateException("Payment failed for order " + order.getId());
        }

        for (OrderItem oi : order.getItems()) {
            productClient.reduceStock(oi.getProductId(), oi.getQuantity());
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaymentId(result.paymentId());
        order = orderRepo.save(order);

        cartService.clear(userId);
        return order;
    }

    public List<Order> listOrders(Long userId) {
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrder(Long userId, Long orderId) {
        return orderRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }
}
