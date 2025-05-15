package com.coffee.coffeestoreapi.controller;

import com.coffee.coffeestoreapi.model.OrderDto;
import com.coffee.coffeestoreapi.model.OrderLine;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/list")
    public ResponseEntity<List<OrderDto>> getOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody OrderRequest lines) {
        //@TODO: Create orderRequest JSON so you can test the functionality
        return orderService.createOrder(lines);
    }
}
