package com.coffee.coffeestoreapi.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Setter
@Getter
public class SimpleOrderDto {
    private String orderNumber;
    private String orderer;
    private LocalDateTime createdAt;
    private Currency currency;
    private List<Discount> discount;
    private Double totalPriceInCents;
    private OrderStatus status;
}
