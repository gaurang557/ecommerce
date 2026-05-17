package com.ecommerce.product.services;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    public ProductRepository repo;

    public List<Product> getAllProducts(){
        return repo.findAll();
    }

    public Page<Product> search(String q, String category, int page, int size){
        String query = (q == null || q.isBlank()) ? null : q;
        String cat = (category == null || category.isBlank()) ? null : category;
        return repo.search(query, cat,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
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
        p.setId(id);
        return repo.save(p);
    }

    @Transactional
    public Product reduceStock(Integer id, int quantity) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        if (p.getStock() == null || p.getStock() < quantity) {
            throw new IllegalStateException("Insufficient stock for product " + id);
        }
        p.setStock(p.getStock() - quantity);
        return repo.save(p);
    }
}
