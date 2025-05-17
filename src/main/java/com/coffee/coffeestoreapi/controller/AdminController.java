package com.coffee.coffeestoreapi.controller;

import com.coffee.coffeestoreapi.model.AdminOrderChangeRequest;
import com.coffee.coffeestoreapi.model.OrderDto;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.model.SimpleOrderDto;
import com.coffee.coffeestoreapi.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/orders")
public class AdminController {
    private final OrderService orderService;

    @Operation(summary = "Get order by orderNumber", description = "Returns a single order based on the provided orderNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content)
    })
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(description = "orderNumber of the order to retrieve", required = true)
            @PathVariable String orderNumber) {
        return orderService.getOrder(orderNumber);
    }

    @Operation(summary = "Get all orders", description = "Returns a list of all orders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of orders retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<List<SimpleOrderDto>> getOrders() {
        return orderService.getAllOrders();
    }

    @PatchMapping("/{orderNumber}")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable String orderNumber, @RequestBody AdminOrderChangeRequest adminOrderChangeRequest) {
        return orderService.updateOrder(orderNumber, adminOrderChangeRequest);
    }

    @DeleteMapping("/{orderNumber}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderNumber) {
        return orderService.deleteOrder(orderNumber);
    }
}
