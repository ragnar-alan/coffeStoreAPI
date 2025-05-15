package com.coffee.coffeestoreapi.controller;

import com.coffee.coffeestoreapi.model.OrderDto;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management API")
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "Get order by ID", description = "Returns a single order based on the provided ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = OrderDto.class))),
        @ApiResponse(responseCode = "404", description = "Order not found", 
                content = @Content)
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(description = "ID of the order to retrieve", required = true) 
            @PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @Operation(summary = "Get all orders", description = "Returns a list of all orders")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of orders retrieved successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = OrderDto.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<List<OrderDto>> getOrders() {
        return orderService.getAllOrders();
    }

    @Operation(summary = "Create a new order", description = "Creates a new order with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input", 
                content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> createOrder(
            @Parameter(description = "Order details", required = true) 
            @RequestBody OrderRequest lines) {
        return orderService.createOrder(lines);
    }
}
