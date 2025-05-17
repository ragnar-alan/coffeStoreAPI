package com.coffee.coffeestoreapi.model;

public enum ProductType {
    DRINK,
    TOPPING;

    public static ProductType fromString(String type) {
        return ProductType.valueOf(type.toUpperCase());
    }
}