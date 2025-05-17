package com.coffee.coffeestoreapi.entity;

import com.coffee.coffeestoreapi.model.ProductType;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "products")
@Entity
public class Product {

    @Id
    @GeneratedValue(generator = "hibernate_sequence")
    @SequenceGenerator(name = "hibernate_sequence", allocationSize = 1)
    private Long id;

    private String productName;

    private Integer priceInCents;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Nullable
    @Column(name ="favorite")
    private Boolean isFavorite;
}