package com.coffee.coffeestoreapi.model;

import lombok.Data;
import java.util.List;

@Data
public class OrderLine {
    private Double priceInCents;
    private List<Item> items;
}
