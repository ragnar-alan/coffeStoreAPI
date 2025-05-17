package com.coffee.coffeestoreapi.entity;

import com.coffee.coffeestoreapi.model.ProductType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "products")
public class Product {

    @Id
    @GeneratedValue(generator = "hibernate_sequence")
    @SequenceGenerator(name = "hibernate_sequence", allocationSize = 1)
    private Long id;

    private String productName;

    private Integer priceInCents;

    @Enumerated(EnumType.STRING)
    private ProductType type;
}