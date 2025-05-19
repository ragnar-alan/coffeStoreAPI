package com.coffee.coffeestoreapi.exception;

public class NoDrinkException extends RuntimeException {
    public NoDrinkException(String message) {
        super(message);
    }
}
