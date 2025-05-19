package com.coffee.coffeestoreapi.service;

import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.exception.OrderNotFoundException;
import com.coffee.coffeestoreapi.mapper.OrderMapper;
import com.coffee.coffeestoreapi.model.AdminOrderChangeRequest;
import com.coffee.coffeestoreapi.model.OrderDto;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.model.PopularItemsDto;
import com.coffee.coffeestoreapi.model.SimpleOrderDto;
import com.coffee.coffeestoreapi.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coffee.coffeestoreapi.model.Currency.EUR;
import static com.coffee.coffeestoreapi.model.OrderStatus.CANCELLED;
import static com.coffee.coffeestoreapi.model.OrderStatus.PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class OrderServiceTest extends BaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderProcessor orderProcessor;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    @DisplayName("getOrder should return order when found")
    void getOrder_ShouldReturnOrder_WhenFound() {
        // Given
        String orderNumber = "RCS-20230101000000000";
        Order order = createTestOrder(orderNumber);
        OrderDto orderDto = createTestOrderDto(orderNumber);
        
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));
        when(orderMapper.orderToOrderDto(order)).thenReturn(orderDto);

        // When
        ResponseEntity<OrderDto> response = orderService.getOrder(orderNumber);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orderDto, response.getBody());
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(orderMapper).orderToOrderDto(order);
    }

    @Test
    @DisplayName("getOrder should return not found when order doesn't exist")
    void getOrder_ShouldReturnNotFound_WhenOrderDoesNotExist() {
        // Given
        String orderNumber = "RCS-20230101000000000";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // When
        ResponseEntity<OrderDto> response = orderService.getOrder(orderNumber);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(orderMapper, never()).orderToOrderDto(any());
    }

    @Test
    @DisplayName("getAllOrders should return all orders with PENDING status")
    void getAllOrders_ShouldReturnAllPendingOrders() {
        // Given
        List<Order> orders = List.of(
            createTestOrder("RCS-20230101000000001"),
            createTestOrder("RCS-20230101000000002")
        );
        
        when(orderRepository.findAllDescendingCreationOrder()).thenReturn(orders);

        // When
        ResponseEntity<List<SimpleOrderDto>> response = orderService.getAllOrders();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("RCS-20230101000000001", response.getBody().get(0).getOrderNumber());
        assertEquals("RCS-20230101000000002", response.getBody().get(1).getOrderNumber());
        verify(orderRepository).findAllDescendingCreationOrder();
    }

    @Test
    @DisplayName("getAllOrders should return empty list when no orders exist")
    void getAllOrders_ShouldReturnEmptyList_WhenNoOrdersExist() {
        // Given
        when(orderRepository.findAllDescendingCreationOrder()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<SimpleOrderDto>> response = orderService.getAllOrders();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(orderRepository).findAllDescendingCreationOrder();
    }

    @Test
    @DisplayName("createOrder should process and save order")
    void createOrder_ShouldProcessAndSaveOrder() {
        // Given
        OrderRequest orderRequest = createOrderRequest("Test Customer", List.of(
            new com.coffee.coffeestoreapi.model.OrderLine(300.0, ESPRESSO, Collections.emptyList())
        ));
        
        Order processedOrder = createTestOrder("RCS-20230101000000000");
        when(orderProcessor.processOrder(orderRequest)).thenReturn(processedOrder);
        when(orderRepository.save(processedOrder)).thenReturn(processedOrder);

        // When
        ResponseEntity<Void> response = orderService.createOrder(orderRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(URI.create("/api/v1/orders/RCS-20230101000000000"), response.getHeaders().getLocation());
        verify(orderProcessor).processOrder(orderRequest);
        verify(orderRepository).save(processedOrder);
    }

    @Test
    @DisplayName("updateOrder should update existing order")
    void updateOrder_ShouldUpdateExistingOrder() {
        // Given
        String orderNumber = "RCS-20230101000000000";
        Order existingOrder = createTestOrder(orderNumber);
        Order updatedOrder = createTestOrder(orderNumber);
        updatedOrder.setOrderer("Updated Customer");
        
        AdminOrderChangeRequest changeRequest = new AdminOrderChangeRequest(
            "Updated Customer",
            List.of(new com.coffee.coffeestoreapi.model.OrderLine(350.0, LATTE, Collections.emptyList()))
        );
        
        OrderDto updatedOrderDto = createTestOrderDto(orderNumber);
        updatedOrderDto.setOrderer("Updated Customer");
        
        when(orderRepository.findByOrderNumberAndStatus(orderNumber, PENDING)).thenReturn(Optional.of(existingOrder));
        when(orderProcessor.processChangedOrder(changeRequest, existingOrder)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.orderToOrderDto(updatedOrder)).thenReturn(updatedOrderDto);

        // When
        ResponseEntity<OrderDto> response = orderService.updateOrder(orderNumber, changeRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedOrderDto, response.getBody());
        verify(orderRepository).findByOrderNumberAndStatus(orderNumber, PENDING);
        verify(orderProcessor).processChangedOrder(changeRequest, existingOrder);
        verify(orderRepository).save(updatedOrder);
        verify(orderMapper).orderToOrderDto(updatedOrder);
    }

    @Test
    @DisplayName("updateOrder should throw exception when order not found")
    void updateOrder_ShouldThrowException_WhenOrderNotFound() {
        // Given
        String orderNumber = "RCS-20230101000000000";
        AdminOrderChangeRequest changeRequest = new AdminOrderChangeRequest(
            "Updated Customer",
            List.of(new com.coffee.coffeestoreapi.model.OrderLine(350.0, LATTE, Collections.emptyList()))
        );
        
        when(orderRepository.findByOrderNumberAndStatus(orderNumber, PENDING)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class, () -> orderService.updateOrder(orderNumber, changeRequest));
        verify(orderRepository).findByOrderNumberAndStatus(orderNumber, PENDING);
        verify(orderProcessor, never()).processChangedOrder(any(), any());
        verify(orderRepository, never()).save(any());
        verify(orderMapper, never()).orderToOrderDto(any());
    }

    @Test
    @DisplayName("deleteOrder should cancel existing order")
    void deleteOrder_ShouldCancelExistingOrder() {
        // Given
        String orderNumber = "RCS-20230101000000000";
        Order existingOrder = createTestOrder(orderNumber);
        
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            assertEquals(CANCELLED, savedOrder.getStatus());
            assertNotNull(savedOrder.getCanceledAt());
            return savedOrder;
        });

        // When
        ResponseEntity<Void> response = orderService.deleteOrder(orderNumber);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("deleteOrder should return not found when order doesn't exist")
    void deleteOrder_ShouldReturnNotFound_WhenOrderDoesNotExist() {
        // Given
        String orderNumber = "RCS-20230101000000000";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = orderService.deleteOrder(orderNumber);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("getMostPopularItems should return popular items")
    void getMostPopularItems_ShouldReturnPopularItems() {
        // Given
        Map<String, Object> popularDrink = new HashMap<>();
        popularDrink.put("name", "Espresso");
        popularDrink.put("count", 10);
        
        Map<String, Object> popularTopping = new HashMap<>();
        popularTopping.put("name", "Milk");
        popularTopping.put("count", 15);
        
        when(orderRepository.findMostPopularDrink()).thenReturn(popularDrink);
        when(orderRepository.findMostPopularTopping()).thenReturn(popularTopping);

        // When
        ResponseEntity<PopularItemsDto> response = orderService.getMostPopularItems();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Espresso", response.getBody().getMostPopularDrink());
        assertEquals(10L, response.getBody().getDrinkCount());
        assertEquals("Milk", response.getBody().getMostPopularTopping());
        assertEquals(15L, response.getBody().getToppingCount());
        verify(orderRepository).findMostPopularDrink();
        verify(orderRepository).findMostPopularTopping();
    }

    @Test
    @DisplayName("getMostPopularItems should handle empty results")
    void getMostPopularItems_ShouldHandleEmptyResults() {
        // Given
        when(orderRepository.findMostPopularDrink()).thenReturn(null);
        when(orderRepository.findMostPopularTopping()).thenReturn(null);

        // When
        ResponseEntity<PopularItemsDto> response = orderService.getMostPopularItems();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No drinks ordered yet", response.getBody().getMostPopularDrink());
        assertEquals(0L, response.getBody().getDrinkCount());
        assertEquals("No toppings ordered yet", response.getBody().getMostPopularTopping());
        assertEquals(0L, response.getBody().getToppingCount());
        verify(orderRepository).findMostPopularDrink();
        verify(orderRepository).findMostPopularTopping();
    }

    // Helper methods
    private Order createTestOrder(String orderNumber) {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber(orderNumber);
        order.setOrderer("Test Customer");
        order.setStatus(PENDING);
        order.setDiscounts(Collections.emptyList());
        order.setSubTotalPriceInCents(300.0);
        order.setTotalPriceInCents(300.0);
        order.setCurrency(EUR);
        order.setOrderLines(List.of(
            new com.coffee.coffeestoreapi.model.OrderLine(300.0, ESPRESSO, Collections.emptyList())
        ));
        order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return order;
    }

    private OrderDto createTestOrderDto(String orderNumber) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setOrderNumber(orderNumber);
        orderDto.setOrderer("Test Customer");
        orderDto.setStatus(PENDING);
        orderDto.setDiscounts(Collections.emptyList());
        orderDto.setSubTotalPriceInCents(300.0);
        orderDto.setTotalPriceInCents(300.0);
        orderDto.setCurrency(EUR);
        orderDto.setOrderLines(List.of(
            new com.coffee.coffeestoreapi.model.OrderLine(300.0, ESPRESSO, Collections.emptyList())
        ));
        orderDto.setCreatedAt(LocalDateTime.now());
        return orderDto;
    }
}