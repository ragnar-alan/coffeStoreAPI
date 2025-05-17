package com.coffee.coffeestoreapi.model;

public enum Currency {
    EUR,
    USD,
    HUF;

    public static Currency fromString(String currency) {
        return Currency.valueOf(currency.toUpperCase());
    }
}