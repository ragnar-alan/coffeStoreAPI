package com.coffee.coffeestoreapi.config.settings;

import lombok.Data;

@Data
public class DiscountSettings {
    private boolean enabled;
    private boolean twentyFivePercent;
    private boolean freeItemAfterThree;
}
