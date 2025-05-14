package com.coffee.coffeestoreapi.model;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    CANCELLED;

    public static OrderStatus fromString(String status) {
        return OrderStatus.valueOf(status.toUpperCase());
    }
}
