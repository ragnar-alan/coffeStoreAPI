package com.coffee.coffeestoreapi.repository;

import com.coffee.coffeestoreapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p ORDER BY p.type ASC")
    List<Product> getAllProducts();


    Optional<Product> findByProductName(String productName);
}