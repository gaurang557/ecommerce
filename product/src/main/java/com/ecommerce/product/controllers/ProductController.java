package com.ecommerce.product.controllers;

import java.util.List;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController()
public class ProductController {

    @Autowired
    public ProductService service;

    @GetMapping("/")
    public List<Product> getAllProducts(){
        return service.getAllProducts();
    }

    @GetMapping("/product/{id}")
    public Product getProductById(@PathVariable Integer id){
        return service.getProductById(id);
    }

    @GetMapping("/delete/{id}")
    public void deleteProductById(@PathVariable Integer id){
        service.deleteProduct(id);
    }
}