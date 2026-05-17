package com.ecommerce.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Component
public class PaymentClient {

    private final RestClient restClient;
    private final String baseUrl;

    public PaymentClient(RestClient restClient,
                         @Value("${services.payment.url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public record ChargeRequest(Long orderId, Long userId, BigDecimal amount) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChargeResult(Long paymentId, String status) {
    }

    public ChargeResult charge(Long orderId, Long userId, BigDecimal amount) {
        try {
            return restClient.post()
                    .uri(baseUrl + "/api/payments/charge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ChargeRequest(orderId, userId, amount))
                    .retrieve()
                    .body(ChargeResult.class);
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException("Payment service error: " + ex.getMessage());
        }
    }
}
