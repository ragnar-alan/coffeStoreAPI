package com.coffee.coffeestoreapi.service;

import com.coffee.coffeestoreapi.mapper.OrderMapper;
import com.coffee.coffeestoreapi.model.OrderDto;
import com.coffee.coffeestoreapi.model.OrderLine;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.repository.OrderRepository;
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

    /**
     * Retrieves an order by its order number.
     *
     * @param orderNumber the unique identifier of the order
     * @return a {@link ResponseEntity} containing the {@link OrderDto} if found,
     *         or a 404 Not Found response if the order does not exist
     */
    public ResponseEntity<OrderDto> getOrder(Long orderNumber) {
        var orderEntity = orderRepository.findById(orderNumber);
        return orderEntity
                .map(order -> ResponseEntity.ok(orderMapper.orderToOrderDto(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all orders in the system.
     *
     * @return a {@link ResponseEntity} containing a list of {@link OrderDto} objects,
     *         or an empty list if no orders exist
     */
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        var orderEntities = orderRepository.findAll();
        if (CollectionUtils.isEmpty(orderEntities)) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(orderMapper.orderListToOrderDtoList(orderEntities));
    }

    /**
     * Creates a new order based on the provided order request.
     * The order is processed, persisted, and a location header is returned.
     *
     * @param orderRequest the order request containing order details
     * @return a {@link ResponseEntity} with a 201 Created status and a location header
     *         pointing to the newly created order resource
     */
    @Transactional
    public ResponseEntity<Void> createOrder(OrderRequest orderRequest) {
        var processedOrder = orderProcessor.processOrder(orderRequest);
        var savedOrder = orderRepository.save(processedOrder);
        return ResponseEntity.created(
                URI.create("/api/v1/orders/%s".formatted(savedOrder.getOrderNumber()))
        ).build();
    }
}
