package com.coffee.coffeestoreapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

//@TODO convert to record
@Data
@NoArgsConstructor
public class OrderDto {
    private String orderNumber;
    private String customerName;
    private String product;
    private int quantity;
    private double price;
}