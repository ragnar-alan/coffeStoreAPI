package com.coffee.coffeestoreapi.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProductCreateRequest(
        @NotEmpty(message = "Product name should not be empty")
        String productName,

        @NotEmpty(message = "Product price in cents should not be empty. It should be a positive integer")
        @Min(value = 1, message = "Product price in cents should not be less than 1 cent")
        Integer priceInCents,

        @NotEmpty(message = "Product type should not be empty. Either DRINK or TOPPING")
        ProductType type,

        Boolean isFavorite
) {}
