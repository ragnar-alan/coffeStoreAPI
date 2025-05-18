package com.coffee.coffeestoreapi.controller;

import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Orders", description = "Order management API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Creates a new order with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input", 
                content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> createOrder(
            @Parameter(description = "Order details", required = true) 
            @Valid @RequestBody OrderRequest request) {
        log.debug("Received request to create an order: {}", request);
        return orderService.createOrder(request);
    }
}
