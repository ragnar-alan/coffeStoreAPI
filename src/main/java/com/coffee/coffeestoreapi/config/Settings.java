package com.coffee.coffeestoreapi.config;

import com.coffee.store.config.settings.DiscountSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
public class Settings {

    @Bean
    @Validated
    @ConfigurationProperties(prefix = "discounts")
    DiscountSettings discountSettings() {
        return new DiscountSettings();
    }
}
