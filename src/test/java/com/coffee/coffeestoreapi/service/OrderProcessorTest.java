package com.coffee.coffeestoreapi.service;

import com.coffee.coffeestoreapi.config.settings.DiscountSettings;
import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.model.AdminOrderChangeRequest;
import com.coffee.coffeestoreapi.model.Discount;
import com.coffee.coffeestoreapi.model.OrderLine;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static com.coffee.coffeestoreapi.model.Currency.EUR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class OrderProcessorTest extends BaseTest {

    @Mock
    private DiscountSettings discountSettings;

    @InjectMocks
    private OrderProcessor orderProcessor;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @ParameterizedTest
    @MethodSource("subtotalTestCases")
    @DisplayName("Should calculate subtotal correctly for different order lines")
    void calculateSubtotalInCents_ShouldCalculateCorrectly(List<OrderLine> orderLines, int expectedSubtotal) {
        // When
        int actualSubtotal = orderProcessor.calculateSubtotalInCents(orderLines);

        // Then
        assertEquals(expectedSubtotal, actualSubtotal, 0.001, "Subtotal calculation is incorrect");
    }

    @ParameterizedTest
    @MethodSource("discountTestCases")
    @DisplayName("Should calculate total discount correctly")
    void calculateTotalDiscount_ShouldCalculateCorrectly(List<Discount> discounts, int subtotalInCents, int expectedDiscount) {
        // When
        int actualDiscount = orderProcessor.calculateTotalDiscount(discounts, subtotalInCents);

        // Then
        assertEquals(expectedDiscount, actualDiscount, 0.001, "Discount calculation is incorrect");
    }

    @ParameterizedTest
    @MethodSource("discountCalculationTestCases")
    @DisplayName("Should calculate applicable discounts correctly")
    void calculateDiscounts_ShouldCalculateCorrectly(List<OrderLine> orderLines, int subtotalInCents, List<Discount> expectedDiscounts) {
        // Given
        when(discountSettings.isEnabled()).thenReturn(true);
        when(discountSettings.isTwentyFivePercent()).thenReturn(true);
        when(discountSettings.isFreeItemAfterThree()).thenReturn(true);

        // When
        List<Discount> actualDiscounts = orderProcessor.calculateDiscounts(orderLines, subtotalInCents);

        // Then
        assertEquals(expectedDiscounts.size(), actualDiscounts.size(), "Number of discounts is incorrect");
        
        if (!expectedDiscounts.isEmpty() && !actualDiscounts.isEmpty()) {
            Discount expectedDiscount = expectedDiscounts.getFirst();
            Discount actualDiscount = actualDiscounts.getFirst();
            
            assertEquals(expectedDiscount.getName(), actualDiscount.getName(), "Discount name is incorrect");
            
            if (expectedDiscount.getPercentage() != null) {
                assertEquals(expectedDiscount.getPercentage(), actualDiscount.getPercentage(), 0.001, "Discount percentage is incorrect");
            } else if (expectedDiscount.getAmountInCents() != null) {
                assertEquals(expectedDiscount.getAmountInCents(), actualDiscount.getAmountInCents(), 0.001, "Discount amount is incorrect");
            }
        }
    }

    @Test
    @DisplayName("Should generate order number in correct format")
    void generateOrderNumber_ShouldGenerateInCorrectFormat() {
        // When
        String orderNumber = orderProcessor.generateOrderNumber();

        // Then
        assertNotNull(orderNumber, "Order number should not be null");
        assertTrue(orderNumber.startsWith("RCS-"), "Order number should start with 'RCS-'");
        assertEquals(21, orderNumber.length(), "Order number should be 21 characters long");
    }

    @ParameterizedTest
    @MethodSource("orderProcessingTestCases")
    @DisplayName("Should process order correctly with different discount settings")
    void processOrder_ShouldProcessCorrectly(OrderRequest orderRequest, boolean discountsEnabled, 
                                            boolean twentyFivePercentEnabled, boolean freeItemAfterThreeEnabled,
                                            int expectedSubtotal, int expectedTotal, int expectedDiscountCount) {
        // Given
        when(discountSettings.isEnabled()).thenReturn(discountsEnabled);
        when(discountSettings.isTwentyFivePercent()).thenReturn(twentyFivePercentEnabled);
        when(discountSettings.isFreeItemAfterThree()).thenReturn(freeItemAfterThreeEnabled);

        // When
        Order order = orderProcessor.processOrder(orderRequest);

        // Then
        assertNotNull(order, "Order should not be null");
        assertNotNull(order.getOrderNumber(), "Order number should not be null");
        assertEquals(OrderStatus.PENDING, order.getStatus(), "Order status should be PENDING");
        assertEquals(orderRequest.orderer(), order.getOrderer(), "Order orderer should match request");
        assertEquals(orderRequest.orderLines(), order.getOrderLines(), "Order lines should match request");
        assertEquals(expectedSubtotal, order.getSubTotalPriceInCents(), 0.001, "Subtotal price is incorrect");
        assertEquals(expectedTotal, order.getTotalPriceInCents(), 0.001, "Total price is incorrect");
        assertEquals(EUR, order.getCurrency(), "Currency should be EUR");
        assertEquals(expectedDiscountCount, order.getDiscounts().size(), "Number of discounts is incorrect");
    }

    @Test
    @DisplayName("Should process changed order correctly")
    void processChangedOrder_ShouldProcessCorrectly() {
        // Given
        OrderRequest originalRequest = createOrderRequest("Original Customer", List.of(
            new OrderLine(300, ESPRESSO, List.of())
        ));
        Order originalOrder = new Order();
        originalOrder.setOrderNumber("RCS-20230101000000000");
        
        String newOrderer = "New Customer";
        List<OrderLine> newOrderLines = List.of(
            new OrderLine(350, LATTE, List.of()),
            new OrderLine(300, CAPPUCCINO, List.of())
        );
        
        var changeRequest = new AdminOrderChangeRequest(
            newOrderer, newOrderLines
        );
        
        when(discountSettings.isEnabled()).thenReturn(true);
        when(discountSettings.isTwentyFivePercent()).thenReturn(true);
        when(discountSettings.isFreeItemAfterThree()).thenReturn(false);

        // When
        Order changedOrder = orderProcessor.processChangedOrder(changeRequest, originalOrder);

        // Then
        assertNotNull(changedOrder, "Changed order should not be null");
        assertEquals("RCS-20230101000000000", changedOrder.getOrderNumber(), "Order number should remain the same");
        assertEquals(newOrderer, changedOrder.getOrderer(), "Order orderer should be updated");
        assertEquals(newOrderLines, changedOrder.getOrderLines(), "Order lines should be updated");
        assertEquals(650, changedOrder.getSubTotalPriceInCents(), "Subtotal price is incorrect");
        assertEquals(EUR, changedOrder.getCurrency(), "Currency should be EUR");
    }


}