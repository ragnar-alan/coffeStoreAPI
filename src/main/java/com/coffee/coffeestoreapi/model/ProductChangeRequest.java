package com.coffee.coffeestoreapi.model;

public record ProductChangeRequest(
        String productName,
        Integer priceInCents,
        ProductType type,
        Boolean isFavorite
) {}
