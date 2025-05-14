package com.coffee.coffeestoreapi.service;

import com.coffee.store.mapper.OrderMapper;
import com.coffee.store.model.OrderDto;
import com.coffee.store.model.OrderRequest;
import com.coffee.store.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderProcessor orderProcessor;

    public ResponseEntity<OrderDto> getOrder(Long orderNumber) {
        var orderEntity = orderRepository.findById(orderNumber);
        return orderEntity
                .map(order -> ResponseEntity.ok(orderMapper.orderToOrderDto(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<List<OrderDto>> getAllOrders() {
        var orderEntities = orderRepository.findAll();
        if (CollectionUtils.isEmpty(orderEntities)) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(orderMapper.orderListToOrderDtoList(orderEntities));
    }

    @Transactional
    public ResponseEntity<Void> createOrder(OrderRequest orderRequest) {
        var processedOrder = orderProcessor.processOrder(orderRequest);
        var savedOrder = orderRepository.save(processedOrder);
        return ResponseEntity.created(
                URI.create("/api/v1/orders/%s".formatted(savedOrder.getOrderNumber()))
        ).build();
    }
}
