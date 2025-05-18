package com.coffee.coffeestoreapi.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminOrderChangeRequest(
        String orderer,

        @NotEmpty(message = "OrderLines should not be empty. To delete an order, please use the delete functionality.")
        List<OrderLine> orderLines
) { }
