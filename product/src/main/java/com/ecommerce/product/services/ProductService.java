package com.ecommerce.product.services;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductService {
    @Autowired
    public ProductRepository repo;
    public List<Product> getAllProducts(){
        return repo.findAll();
    }

    public Product getProductById(Integer id){
        return repo.findById(id).orElse(null);
    }

    public Product createProduct(Product p) {
        return repo.save(p);
    }

    public void deleteProduct(Integer id){
        repo.deleteById(id);
    }

    public Product updateProduct(Integer id, Product p) {
        return repo.save(p);
    }
}
