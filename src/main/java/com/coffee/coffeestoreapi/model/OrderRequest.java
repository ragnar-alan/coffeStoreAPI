package com.coffee.coffeestoreapi.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderRequest(
        @NotNull(message = "Price should not be null")
        int totalPriceInCents,

        @Max(value = 50, message = "Orderer name should not exceed 50 characters")
        @NotEmpty(message = "Orderer name should not be empty")
        String orderer,

        Currency currency,

        @NotNull(message = "Order lines should not be null")
        List<OrderLine> orderLines
) { }
