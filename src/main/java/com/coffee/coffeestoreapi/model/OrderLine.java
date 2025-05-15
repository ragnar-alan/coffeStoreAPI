package com.coffee.coffeestoreapi.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderLine(
        Double priceInCents,
        List<Coffee> coffees,
        List<Topping> toppings
) {
}
