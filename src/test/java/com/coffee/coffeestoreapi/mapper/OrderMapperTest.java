package com.coffee.coffeestoreapi.mapper;

import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.model.Currency;
import com.coffee.coffeestoreapi.model.Discount;
import com.coffee.coffeestoreapi.model.OrderDto;
import com.coffee.coffeestoreapi.model.OrderLine;
import com.coffee.coffeestoreapi.model.OrderStatus;
import com.coffee.coffeestoreapi.service.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderMapperTest extends BaseTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    @DisplayName("orderToOrderDto should correctly map Order to OrderDto")
    void orderToOrderDto_ShouldCorrectlyMapOrderToOrderDto() {
        // Given
        Order order = createTestOrder();

        // When
        OrderDto orderDto = orderMapper.orderToOrderDto(order);

        // Then
        assertNotNull(orderDto);
        assertEquals(order.getId(), orderDto.getId());
        assertEquals(order.getOrderNumber(), orderDto.getOrderNumber());
        assertEquals(order.getOrderer(), orderDto.getOrderer());
        assertEquals(order.getStatus(), orderDto.getStatus());
        assertEquals(order.getSubTotalPriceInCents(), orderDto.getSubTotalPriceInCents());
        assertEquals(order.getTotalPriceInCents(), orderDto.getTotalPriceInCents());
        assertEquals(order.getCurrency(), orderDto.getCurrency());
        assertEquals(order.getOrderLines().size(), orderDto.getOrderLines().size());
        assertEquals(order.getDiscounts().size(), orderDto.getDiscounts().size());

        // Verify date format conversion
        // Compare year, month, day, hour, minute, second only (ignore millisecond precision)
        LocalDateTime expectedCreatedAt = order.getCreatedAt().toLocalDateTime();
        assertEquals(expectedCreatedAt.getYear(), orderDto.getCreatedAt().getYear());
        assertEquals(expectedCreatedAt.getMonth(), orderDto.getCreatedAt().getMonth());
        assertEquals(expectedCreatedAt.getDayOfMonth(), orderDto.getCreatedAt().getDayOfMonth());
        assertEquals(expectedCreatedAt.getHour(), orderDto.getCreatedAt().getHour());
        assertEquals(expectedCreatedAt.getMinute(), orderDto.getCreatedAt().getMinute());
        assertEquals(expectedCreatedAt.getSecond(), orderDto.getCreatedAt().getSecond());

        assertEquals(order.getUpdatedAt(), orderDto.getUpdatedAt());
        assertEquals(order.getProcessedAt(), orderDto.getProcessedAt());
        assertEquals(order.getCompletedAt(), orderDto.getCompletedAt());
    }

    @Test
    @DisplayName("orderToOrderDto should handle null Order")
    void orderToOrderDto_ShouldHandleNullOrder() {
        // When
        OrderDto orderDto = orderMapper.orderToOrderDto(null);

        // Then
        assertNull(orderDto);
    }

    @Test
    @DisplayName("orderToOrderDto should handle Order with null fields")
    void orderToOrderDto_ShouldHandleOrderWithNullFields() {
        // Given
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-123");

        // When
        OrderDto orderDto = orderMapper.orderToOrderDto(order);

        // Then
        assertNotNull(orderDto);
        assertEquals(1L, orderDto.getId());
        assertEquals("ORD-123", orderDto.getOrderNumber());
        assertNull(orderDto.getOrderer());
        assertNull(orderDto.getStatus());
        assertNull(orderDto.getSubTotalPriceInCents());
        assertNull(orderDto.getTotalPriceInCents());
        assertNull(orderDto.getCurrency());
        assertNull(orderDto.getOrderLines());
        assertNull(orderDto.getDiscounts());
        assertNull(orderDto.getCreatedAt());
        assertNull(orderDto.getUpdatedAt());
        assertNull(orderDto.getProcessedAt());
        assertNull(orderDto.getCompletedAt());
        assertNull(orderDto.getCanceledAt());
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-123");
        order.setOrderer("John Doe");
        order.setStatus(OrderStatus.PENDING);
        order.setSubTotalPriceInCents(1000);
        order.setTotalPriceInCents(800);
        order.setCurrency(Currency.EUR);

        List<OrderLine> orderLines = Arrays.asList(
            new OrderLine(500, ESPRESSO, Arrays.asList(MILK, SUGAR)),
            new OrderLine(500, LATTE, Arrays.asList(CINNAMON))
        );
        order.setOrderLines(orderLines);

        List<Discount> discounts = Arrays.asList(
            createPercentageDiscount("25% off for orders over €12", 25)
        );
        order.setDiscounts(discounts);

        // Set timestamps
        order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        order.setUpdatedAt(LocalDate.now());
        order.setProcessedAt(LocalDate.now());
        order.setCompletedAt(LocalDate.now());

        return order;
    }
}
