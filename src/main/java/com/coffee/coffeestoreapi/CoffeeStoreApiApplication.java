package com.coffee.coffeestoreapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CoffeeStoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeStoreApiApplication.class, args);
    }

}
