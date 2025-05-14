package com.coffee.coffeestoreapi;

import org.springframework.boot.SpringApplication;

public class TestCoffeeStoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(CoffeeStoreApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
