package com.coffee.coffeestoreapi.controller.admin;

import com.coffee.coffeestoreapi.model.AdminOrderChangeRequest;
import com.coffee.coffeestoreapi.model.OrderDto;
import com.coffee.coffeestoreapi.model.SimpleOrderDto;
import com.coffee.coffeestoreapi.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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
public class AdminOrderController {
    private final OrderService orderService;


    @Operation(
            summary = "Get order by order number",
            description = "Retrieves the details of a specific order by its order number."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(description = "orderNumber of the order to retrieve", required = true)
            @PathVariable String orderNumber) {
        return orderService.getOrder(orderNumber);
    }

    @Operation(
            summary = "List all orders",
            description = "Retrieves a list of all orders with basic information."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of orders",
                    content = @Content(schema = @Schema(implementation = SimpleOrderDto.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<List<SimpleOrderDto>> getOrders() {
        return orderService.getAllOrders();
    }

    @Operation(
            summary = "Update an order",
            description = "Updates an existing order with the provided changes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PatchMapping("/{orderNumber}")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable String orderNumber, @Valid @RequestBody AdminOrderChangeRequest adminOrderChangeRequest) {
        return orderService.updateOrder(orderNumber, adminOrderChangeRequest);
    }

    @Operation(
            summary = "Delete an order",
            description = "Deletes an order by its order number."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{orderNumber}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderNumber) {
        return orderService.deleteOrder(orderNumber);
    }
}
