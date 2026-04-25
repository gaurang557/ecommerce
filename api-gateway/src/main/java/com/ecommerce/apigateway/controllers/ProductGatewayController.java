package com.ecommerce.apigateway.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/products")
public class ProductGatewayController {

    private final RestClient restClient;
    private final String productServiceUrl;

    public ProductGatewayController(
            RestClient restClient,
            @Value("${services.product.url:http://product:8081}") String productServiceUrl
    ) {
        this.restClient = restClient;
        this.productServiceUrl = productServiceUrl;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<String> getAllProducts() {
        return restClient.get()
                .uri(productServiceUrl + "/")
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<String> getProductById(@PathVariable Integer id) {
        return restClient.get()
                .uri(productServiceUrl + "/product/" + id)
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/product")
    public ResponseEntity<String> createProduct(@RequestBody String body) {
        return restClient.post()
                .uri(productServiceUrl + "/product")
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Integer id, @RequestBody String body) {
        return restClient.put()
                .uri(productServiceUrl + "/product/" + id)
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        return restClient.delete()
                .uri(productServiceUrl + "/product/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}
