package com.coffee.coffeestoreapi.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderRequest(

        @NotNull(message = "Price should not be empty or null")
        /*@Min(value = 0, message = "Price should be greater than or equal to 0")*/
        Integer totalPriceInCents,

        @Size(max = 50, message = "Orderer name should not exceed 50 characters")
        @NotEmpty(message = "Orderer name should not be empty")
        String orderer,

        Currency currency,

        @NotEmpty(message = "OrderLines should not be empty.")
        List<OrderLine> orderLines
) { }
