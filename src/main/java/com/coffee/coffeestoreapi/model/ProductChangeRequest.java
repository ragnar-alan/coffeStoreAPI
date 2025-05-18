package com.coffee.coffeestoreapi.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record ProductChangeRequest(
        String productName,
        @NotNull(message = "Product price in cents should not be null. It should be a positive integer")
        @Min(value = 1, message = "Product price in cents should not be less than 1 cent")
        Integer priceInCents,
        ProductType type,
        Boolean isFavorite
) {}
