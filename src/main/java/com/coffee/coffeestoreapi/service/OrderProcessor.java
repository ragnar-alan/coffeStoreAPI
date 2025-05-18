package com.coffee.coffeestoreapi.service;

import com.coffee.coffeestoreapi.config.settings.DiscountSettings;
import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.model.AdminOrderChangeRequest;
import com.coffee.coffeestoreapi.model.Currency;
import com.coffee.coffeestoreapi.model.Discount;
import com.coffee.coffeestoreapi.model.OrderLine;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProcessor {
    private final DiscountSettings discountSettings;

    /**
     * Processes an order request, calculates subtotal, applies discounts if enabled,
     * and returns a populated {@link Order} entity.
     *
     * @param orderRequest the order request containing order lines
     * @return the processed {@link Order} with calculated prices and discounts
     */
    public Order processOrder(OrderRequest orderRequest) {
        var order = new Order();
        order.setOrderNumber(generateOrderNumber());
        return populateOrder(orderRequest.orderer(), orderRequest.orderLines(), order);
    }

    public Order processChangedOrder(AdminOrderChangeRequest request, Order order) {
        return populateOrder(request.orderer(), request.orderLines(), order);
    }

    /**
     * Calculates the subtotal price in cents for the given order lines.
     *
     * @param orderLines the list of order lines
     * @return the subtotal price in cents
     */
    protected double calculateSubtotalInCents(List<OrderLine> orderLines) {
        if (CollectionUtils.isEmpty(orderLines)) {
            return 0.0;
        }

        return orderLines.stream()
                .mapToDouble(OrderLine::priceInCents)
                .sum();
    }

    /**
     * Calculates the total discount amount in cents for the given discounts and subtotal.
     * Only the discount with the highest value is applied.
     *
     * @param discounts the list of applicable discounts
     * @param subtotalInCents the subtotal price in cents
     * @return the total discount amount in cents
     */
    protected double calculateTotalDiscount(List<Discount> discounts, double subtotalInCents) {
        if (CollectionUtils.isEmpty(discounts)) {
            return 0.0;
        }

        // Find the discount that results in the lowest cart amount
        return discounts.stream()
                .mapToDouble(discount -> {
                    if (discount.getPercentage() != null) {
                        return subtotalInCents * discount.getPercentage() / 100.0;
                    } else if (discount.getAmountInCents() != null) {
                        return discount.getAmountInCents();
                    }
                    return 0.0;
                })
                .max()
                .orElse(0.0);
    }

    /**
     * Determines the applicable discounts for the given order lines and subtotal.
     * Returns only the discount(s) that result in the lowest cart amount.
     *
     * @param lines the list of order lines
     * @param subtotalInCents the subtotal price in cents
     * @return the list of applicable discounts
     */
    protected List<Discount> calculateDiscounts(List<OrderLine> lines, double subtotalInCents) {
        if (CollectionUtils.isEmpty(lines)) {
            return List.of();
        }

        List<Discount> discounts = new ArrayList<>();

        // 1. If the total cost of the cart is more than 12 euros, there should be a 25% discount.
        quarterDiscountCalculation(subtotalInCents, discounts);

        // 2. If there are 3 or more drink in the cart, the one with the lowest amount should be free.
        freeItemAfterThreeDiscountCalculation(lines, discounts);

        // 3. If eligible for both promotions, use the one with the lowest cart amount (highest discount value)
        List<Discount> possibleDiscounts = calculatePossibleDiscountOnOrder(subtotalInCents, discounts);
        return possibleDiscounts != null ? possibleDiscounts : discounts;
    }

    /**
     * Generates a unique order number in the format RCS-yyyyMMddHHmmssSSS.
     *
     * @return the generated order number
     */
    protected String generateOrderNumber() {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "RCS-" + timestamp;
    }

    private static List<Discount> calculatePossibleDiscountOnOrder(double subtotalInCents, List<Discount> possibleDiscounts) {
        if (possibleDiscounts.size() > 1) {
            // Calculate the actual discount amount for each discount
            double percentageDiscountAmount = subtotalInCents * 0.25; // 25% discount
            double freeItemDiscountAmount = possibleDiscounts.stream()
                    .filter(discount -> discount.getAmountInCents() != null)
                    .mapToDouble(Discount::getAmountInCents)
                    .findFirst()
                    .orElse(0.0);

            // Keep only the discount that results in the lowest cart amount (lowest discount value)
            if (percentageDiscountAmount >= freeItemDiscountAmount) {
                return possibleDiscounts.stream()
                        .filter(discount -> discount.getAmountInCents() != null)
                        .toList();
            } else {
                return possibleDiscounts.stream()
                        .filter(discount -> discount.getPercentage() != null)
                        .toList();
            }
        }
        return null;
    }

    private void freeItemAfterThreeDiscountCalculation(List<OrderLine> lines, List<Discount> possibleDiscounts) {
        List<OrderLine> drinkLines = lines.stream()
                .filter(line -> line.drink() != null)
                .toList();

        if (discountSettings.isFreeItemAfterThree() && drinkLines.size() >= 3) {
            // Find the drink with the lowest price
            OrderLine cheapestDrinkLine = drinkLines.stream()
                    .min(Comparator.comparing(OrderLine::priceInCents))
                    .orElse(null);

            if (cheapestDrinkLine != null) {
                Discount discount = new Discount();
                discount.setName("Free drink for 3+ drink in cart");
                discount.setAmountInCents(cheapestDrinkLine.priceInCents());
                possibleDiscounts.add(discount);
            }
        }
    }

    private void quarterDiscountCalculation(double subtotalInCents, List<Discount> possibleDiscounts) {
        if (discountSettings.isTwentyFivePercent() && subtotalInCents > 1200) { // 12 euros in cents
            Discount discount = new Discount();
            discount.setName("25% off for orders over €12");
            discount.setPercentage(25.0);
            discount.setAmountInCents(subtotalInCents * 0.25);
            possibleDiscounts.add(discount);
        }
    }

    private Order populateOrder(String orderer, List<OrderLine> orderLines, Order order) {
        order.setOrderer(orderer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderLines(orderLines);

        double subtotalInCents = calculateSubtotalInCents(orderLines);
        order.setSubTotalPriceInCents(subtotalInCents);

        if (discountSettings.isEnabled()) {
            List<Discount> discounts = calculateDiscounts(orderLines, subtotalInCents);
            order.setDiscounts(discounts);
            double totalDiscount = calculateTotalDiscount(discounts, subtotalInCents);
            order.setTotalPriceInCents(subtotalInCents - totalDiscount);
        } else {
            order.setDiscounts(List.of());
            order.setTotalPriceInCents(subtotalInCents);
        }

        order.setCurrency(Currency.EUR);
        return order;
    }
}
