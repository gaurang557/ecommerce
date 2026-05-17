package com.ecommerce.payment.controllers;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.repositories.PaymentRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository repo;

    public PaymentController(PaymentRepository repo) {
        this.repo = repo;
    }

    public record ChargeRequest(@NotNull Long orderId, @NotNull Long userId,
                                @NotNull BigDecimal amount) {
    }

    public record ChargeResult(Long paymentId, String status) {
    }

    @PostMapping("/charge")
    public ResponseEntity<ChargeResult> charge(@RequestBody ChargeRequest req) {
        boolean approved = req.amount() != null
                && req.amount().compareTo(BigDecimal.ZERO) > 0;

        Payment payment = new Payment();
        payment.setOrderId(req.orderId());
        payment.setUserId(req.userId());
        payment.setAmount(req.amount());
        payment.setStatus(approved ? "SUCCESS" : "FAILED");
        repo.save(payment);

        ChargeResult result = new ChargeResult(payment.getId(), payment.getStatus());
        return approved
                ? ResponseEntity.ok(result)
                : ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handle(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
