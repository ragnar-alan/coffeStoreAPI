package com.coffee.coffeestoreapi.service;

import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.exception.OrderNotFoundException;
import com.coffee.coffeestoreapi.mapper.OrderMapper;
import com.coffee.coffeestoreapi.model.AdminOrderChangeRequest;
import com.coffee.coffeestoreapi.model.OrderDto;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.model.OrderStatus;
import com.coffee.coffeestoreapi.model.PopularItemsDto;
import com.coffee.coffeestoreapi.model.SimpleOrderDto;
import com.coffee.coffeestoreapi.repository.OrderRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static com.coffee.coffeestoreapi.model.OrderStatus.PENDING;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderProcessor orderProcessor;

    /**
     * Retrieves an order by its order number.
     *
     * @param orderNumber the unique identifier of the order
     * @return a {@link ResponseEntity} containing the {@link OrderDto} if found,
     * or a 404 Not Found response if the order does not exist
     */
    public ResponseEntity<OrderDto> getOrder(String orderNumber) {
        var orderEntity = orderRepository.findByOrderNumber(orderNumber);
        return orderEntity
                .map(order -> ResponseEntity.ok(orderMapper.orderToOrderDto(order)))
                .orElseGet(() -> {
                    log.error("Order not found when getting the order with the given order number: {}", orderNumber);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Retrieves all orders in the system.
     *
     * @return a {@link ResponseEntity} containing a list of {@link OrderDto} objects,
     * or an empty list if no orders exist
     */
    public ResponseEntity<List<SimpleOrderDto>> getAllOrders() {
        var orderEntities = orderRepository.findAllByStatusDescendingCreationOrder(PENDING);
        if (CollectionUtils.isEmpty(orderEntities)) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(mapOrdersToSimpleOrderDtos(orderEntities));
    }

    /**
     * Creates a new order based on the provided order request.
     * The order is processed, persisted, and a location header is returned.
     *
     * @param orderRequest the order request containing order details
     * @return a {@link ResponseEntity} with a 201 Created status and a location header
     * pointing to the newly created order resource
     */
    @Transactional
    public ResponseEntity<Void> createOrder(OrderRequest orderRequest) {
        var processedOrder = orderProcessor.processOrder(orderRequest);
        var savedOrder = orderRepository.save(processedOrder);
        return ResponseEntity.created(
                URI.create("/api/v1/orders/%s".formatted(savedOrder.getOrderNumber()))
        ).build();
    }

    @Transactional
    public ResponseEntity<OrderDto> updateOrder(String orderNumber, AdminOrderChangeRequest adminOrderChangeRequest) {
        var orderOpt = orderRepository.findByOrderNumberAndStatus(orderNumber, PENDING);
        var order = orderOpt.orElseThrow(() -> {
            log.error("Order not found when updating the order with the given order number: {} and status: {}", orderNumber, PENDING);
            return new OrderNotFoundException("Order not found when updating the order with the given order number: %s".formatted(orderNumber));
        });
        var processedOrder = orderProcessor.processChangedOrder(adminOrderChangeRequest, order);
        return ResponseEntity.ok(orderMapper.orderToOrderDto(orderRepository.save(processedOrder)));

        //I could implement a credit if the order total amount changed both directions.
        // a store credit if the order amount decreased or a payment request
        // if the order amount increased in the real world
    }

    @Transactional
    public ResponseEntity<Void> deleteOrder(String orderNumber) {
        var orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            var order = orderOpt.get();
            order.setCanceledAt(new Timestamp(System.currentTimeMillis()));
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return ResponseEntity.noContent().build();
        }

        log.warn("Order not found when deleting the order with the given order number: {}", orderNumber);
        return ResponseEntity.notFound().build();
    }

    /**
     * Retrieves the most popular drink and topping across all orders.
     *
     * @return a {@link ResponseEntity} containing the {@link PopularItemsDto} with information
     * about the most popular drink and topping
     */
    @Transactional
    public ResponseEntity<PopularItemsDto> getMostPopularItems() {
        Map<String, Object> mostPopularDrink = orderRepository.findMostPopularDrink();
        Map<String, Object> mostPopularTopping = orderRepository.findMostPopularTopping();

        // Handle case when there are no orders yet
        if (mostPopularDrink == null || mostPopularDrink.isEmpty() ||
                mostPopularTopping == null || mostPopularTopping.isEmpty()) {
            log.info("No orders found when getting the most popular items");
            return ResponseEntity.ok(
                    PopularItemsDto.builder()
                            .mostPopularDrink("No drinks ordered yet")
                            .drinkCount(0L)
                            .mostPopularTopping("No toppings ordered yet")
                            .toppingCount(0L)
                            .build()
            );
        }

        String drinkName = (String) mostPopularDrink.get("name");
        Long drinkCount = Long.valueOf(mostPopularDrink.get("count").toString());

        String toppingName = (String) mostPopularTopping.get("name");
        Long toppingCount = Long.valueOf(mostPopularTopping.get("count").toString());

        return ResponseEntity.ok(
                PopularItemsDto.builder()
                        .mostPopularDrink(drinkName)
                        .drinkCount(drinkCount)
                        .mostPopularTopping(toppingName)
                        .toppingCount(toppingCount)
                        .build()
        );
    }

    private List<SimpleOrderDto> mapOrdersToSimpleOrderDtos(List<Order> orderEntities) {
        return orderEntities.stream()
                .map(OrderService::getSimpleOrderDto)
                .toList();
    }

    private static SimpleOrderDto getSimpleOrderDto(Order order) {
        return SimpleOrderDto.builder()
                .orderNumber(order.getOrderNumber())
                .orderer(order.getOrderer())
                .createdAt(order.getCreatedAt().toLocalDateTime())
                .currency(order.getCurrency())
                .totalPriceInCents(order.getTotalPriceInCents())
                .discount(order.getDiscounts())
                .build();
    }
}
