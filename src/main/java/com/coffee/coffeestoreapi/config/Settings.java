package com.coffee.coffeestoreapi.config;

import com.coffee.coffeestoreapi.config.settings.DiscountSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Settings {

    @Bean
    @ConfigurationProperties(prefix = "discounts")
    DiscountSettings discountSettings() {
        return new DiscountSettings();
    }
}
