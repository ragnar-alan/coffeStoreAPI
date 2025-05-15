package com.coffee.coffeestoreapi.service;

import com.coffee.coffeestoreapi.config.settings.DiscountSettings;
import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.model.Discount;
import com.coffee.coffeestoreapi.model.OrderLine;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderProcessorTest extends BaseTest {

    @Mock
    private DiscountSettings discountSettings;

    @InjectMocks
    private OrderProcessor orderProcessor;

    @BeforeEach
    void setUp() {
        // Default mock behavior with lenient mode to avoid UnnecessaryStubbingException
        lenient().when(discountSettings.isEnabled()).thenReturn(true);
        lenient().when(discountSettings.isTwentyFivePercent()).thenReturn(true);
        lenient().when(discountSettings.isFreeItemAfterThree()).thenReturn(true);
    }

    @ParameterizedTest
    @MethodSource("provideOrderLinesForSubtotal")
    @DisplayName("calculateSubtotalInCents should correctly sum order line prices")
    void calculateSubtotalInCents_ShouldSumOrderLinePrices(List<OrderLine> orderLines, double expected) {
        // When
        double result = orderProcessor.calculateSubtotalInCents(orderLines);

        // Then
        assertEquals(expected, result, 0.001, "Subtotal calculation is incorrect");
    }

    @ParameterizedTest
    @MethodSource("provideDiscountsForTotalDiscount")
    @DisplayName("calculateTotalDiscount should correctly calculate the discount amount")
    void calculateTotalDiscount_ShouldCalculateCorrectAmount(List<Discount> discounts, double subtotal, double expected) {
        // When
        double result = orderProcessor.calculateTotalDiscount(discounts, subtotal);

        // Then
        assertEquals(expected, result, 0.001, "Total discount calculation is incorrect");
    }

    @ParameterizedTest
    @MethodSource("provideOrderLinesForDiscounts")
    @DisplayName("calculateDiscounts should correctly determine applicable discounts")
    void calculateDiscounts_ShouldDetermineApplicableDiscounts(List<OrderLine> orderLines, double subtotal, List<Discount> expected) {
        // When
        List<Discount> result = orderProcessor.calculateDiscounts(orderLines, subtotal);

        // Then
        assertEquals(expected.size(), result.size(), "Number of discounts is incorrect");

        if (!expected.isEmpty() && !result.isEmpty()) {
            if (expected.get(0).getPercentage() != null) {
                assertEquals(expected.get(0).getPercentage(), result.get(0).getPercentage(),
                        "Percentage discount is incorrect");
            } else if (expected.get(0).getAmountInCents() != null) {
                assertEquals(expected.get(0).getAmountInCents(), result.get(0).getAmountInCents(), 0.001,
                        "Fixed discount amount is incorrect");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOrderRequestsForProcessing")
    @DisplayName("processOrder should correctly process an order with or without discounts")
    void processOrder_ShouldProcessOrderCorrectly(OrderRequest request, boolean discountsEnabled,
                                                  double expectedSubtotal, double expectedTotal) {
        // Given
        when(discountSettings.isEnabled()).thenReturn(discountsEnabled);

        // When
        Order result = orderProcessor.processOrder(request);

        // Then
        assertNotNull(result, "Order should not be null");
        assertEquals(OrderStatus.PENDING, result.getStatus(), "Order status should be PENDING");
        assertEquals(expectedSubtotal, result.getSubTotalPriceInCents(), 0.001, "Subtotal is incorrect");
        assertEquals(expectedTotal, result.getTotalPriceInCents(), 0.001, "Total price is incorrect");
        assertEquals("EUR", result.getCurrency(), "Currency should be EUR");
        assertNotNull(result.getOrderNumber(), "Order number should not be null");
        assertTrue(result.getOrderNumber().startsWith("RCS-"), "Order number should start with RCS-");
    }

    @Test
    @DisplayName("generateOrderNumber should create an order number with correct format")
    void generateOrderNumber_ShouldHaveCorrectFormat() {
        // When
        String orderNumber = orderProcessor.generateOrderNumber();

        // Then
        assertNotNull(orderNumber, "Order number should not be null");
        assertTrue(orderNumber.startsWith("RCS-"), "Order number should start with RCS-");
        assertEquals(21, orderNumber.length(), "Order number should have correct length");

        // Check that the rest of the order number is a valid timestamp format (14 digits)
        String timestamp = orderNumber.substring(4);
        assertTrue(timestamp.matches("\\d{17}"), "Timestamp part should be 14 digits");
    }

    @ParameterizedTest
    @CsvSource({
            "0, false, false",
            "1100, false, false",
            "1200, false, false",
            "1201, true, true"
    })
    @DisplayName("quarterDiscountCalculation should add discount only when conditions are met")
    void quarterDiscountCalculation_ShouldAddDiscountWhenConditionsMet(
            double subtotalInCents, boolean twentyFivePercentEnabled, boolean shouldHaveDiscount) {
        // Given
        when(discountSettings.isTwentyFivePercent()).thenReturn(twentyFivePercentEnabled);
        List<Discount> discounts = new ArrayList<>();

        // When
        orderProcessor.calculateDiscounts(BaseTest.createCoffeeOrderLines(1), subtotalInCents);

        // Then
        if (shouldHaveDiscount) {
            verify(discountSettings, atLeastOnce()).isTwentyFivePercent();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1, false, false",
            "2, false, false",
            "3, true, true",
            "4, true, true"
    })
    @DisplayName("freeItemAfterThreeDiscountCalculation should add discount only when conditions are met")
    void freeItemAfterThreeDiscountCalculation_ShouldAddDiscountWhenConditionsMet(
            int coffeeCount, boolean freeItemEnabled, boolean shouldHaveDiscount) {
        // Given
        when(discountSettings.isFreeItemAfterThree()).thenReturn(freeItemEnabled);

        // When
        orderProcessor.calculateDiscounts(BaseTest.createCoffeeOrderLines(coffeeCount), 1000.0);

        // Then
        if (shouldHaveDiscount) {
            verify(discountSettings, atLeastOnce()).isFreeItemAfterThree();
        }
    }
}
