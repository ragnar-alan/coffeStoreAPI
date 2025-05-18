package com.coffee.coffeestoreapi.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object for representing the most popular drink and topping.
 */
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PopularItemsDto {
    private String mostPopularDrink;
    private Long drinkCount;
    private String mostPopularTopping;
    private Long toppingCount;
}