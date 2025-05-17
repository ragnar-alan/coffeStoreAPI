package com.coffee.coffeestoreapi.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderRequest(
        int totalPriceInCents,

        @Max(value = 50, message = "Orderer name should not exceed 50 characters")
        String orderer,

        Currency currency,
        List<OrderLine> orderLines
) { }
