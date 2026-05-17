package com.ecommerce.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Component
public class ProductClient {

    private final RestClient restClient;
    private final String baseUrl;

    public ProductClient(RestClient restClient,
                         @Value("${services.product.url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductDto(Integer id, String name, BigDecimal price, Integer stock) {
    }

    public ProductDto getProduct(Integer productId) {
        try {
            return restClient.get()
                    .uri(baseUrl + "/product/" + productId)
                    .retrieve()
                    .body(ProductDto.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Product not found: " + productId);
            }
            throw new IllegalStateException("Product service error: " + ex.getMessage());
        }
    }

    public void reduceStock(Integer productId, int quantity) {
        try {
            restClient.put()
                    .uri(baseUrl + "/product/" + productId + "/reduce-stock?quantity=" + quantity)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "Could not reserve stock for product " + productId + ": " + ex.getMessage());
        }
    }
}
