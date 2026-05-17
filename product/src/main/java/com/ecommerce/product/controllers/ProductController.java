package com.ecommerce.product.controllers;

import java.util.List;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @Autowired
    public ProductService service;

    @GetMapping("/")
    public ResponseEntity<List<Product>> getAllProducts(){
        return ResponseEntity.ok(service.getAllProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(service.search(q, category, page, size));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id){
        Product product = service.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Integer id){
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/product")
    public ResponseEntity<Product> createProduct(@RequestBody Product product){
        return ResponseEntity.ok(service.createProduct(product));
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product){
        return ResponseEntity.ok(service.updateProduct(id, product));
    }

    @PutMapping("/product/{id}/reduce-stock")
    public ResponseEntity<Product> reduceStock(@PathVariable Integer id,
                                               @RequestParam int quantity){
        return ResponseEntity.ok(service.reduceStock(id, quantity));
    }
}
