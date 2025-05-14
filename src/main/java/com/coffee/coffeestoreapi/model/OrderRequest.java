package com.coffee.coffeestoreapi.model;

import java.util.List;

public record OrderRequest(
        List<OrderLine> orderLine
) { }
