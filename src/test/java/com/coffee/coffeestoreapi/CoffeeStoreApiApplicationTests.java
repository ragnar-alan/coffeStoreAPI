package com.coffee.coffeestoreapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CoffeeStoreApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
