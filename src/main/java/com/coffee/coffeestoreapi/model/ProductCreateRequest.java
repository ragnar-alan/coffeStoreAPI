package com.coffee.coffeestoreapi.model;

public record ProductCreateRequest(
        String productName,
        Integer priceInCents,
        ProductType type,
        Boolean isFavorite
) {}
