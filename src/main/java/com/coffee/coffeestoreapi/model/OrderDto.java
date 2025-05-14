package com.coffee.coffeestoreapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private List<Discount> discounts;
    private Double subTotalPriceInCents;
    private Double totalPriceInCents;
    private String currency;
    private List<OrderLine> orderLines;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private LocalDate processedAt;
    private LocalDate completedAt;
    private LocalDate canceledAt;
}