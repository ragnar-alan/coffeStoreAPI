package com.coffee.coffeestoreapi.model;

import lombok.Data;

@Data
public class Discount {
    private String name;
    private Double percentage;
    private Double amountInCents;
}
