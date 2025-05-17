package com.coffee.coffeestoreapi.model;


public record ProductDto(
        Long id,
        String productName,
        Integer priceInCents,
        ProductType type,
        Boolean isFavorite
) {
}
