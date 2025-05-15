package com.coffee.coffeestoreapi.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderRequest(
        int totalPriceInCents,
        String currency,
        List<OrderLine> orderLines,
        LocalDate createdAt
) { }
