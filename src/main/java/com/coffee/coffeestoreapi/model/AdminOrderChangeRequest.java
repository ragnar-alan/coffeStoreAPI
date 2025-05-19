package com.coffee.coffeestoreapi.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminOrderChangeRequest(

        @Size(max = 50, message = "Orderer name should not exceed 50 characters")
        @NotEmpty(message = "Orderer name should not be empty")
        String orderer,

        @NotEmpty(message = "OrderLines should not be empty.")
        List<OrderLine> orderLines
) { }
