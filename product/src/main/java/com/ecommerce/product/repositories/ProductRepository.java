package com.ecommerce.product.repositories;

import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
            SELECT p FROM Product p
            WHERE (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
            """)
    Page<Product> search(@Param("q") String q,
                         @Param("category") String category,
                         Pageable pageable);
}
