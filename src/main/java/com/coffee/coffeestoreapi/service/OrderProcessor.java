package com.coffee.coffeestoreapi.service;

import com.coffee.coffeestoreapi.config.settings.DiscountSettings;
import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.model.Coffee;
import com.coffee.coffeestoreapi.model.Discount;
import com.coffee.coffeestoreapi.model.OrderLine;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderProcessor {
    private final DiscountSettings discountSettings;

    public Order processOrder(OrderRequest orderRequest) {
        var order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderLines(orderRequest.orderLines());

        // Calculate subtotal price (sum of all items)
        double subtotalInCents = calculateSubtotalInCents(orderRequest.orderLines());
        order.setSubTotalPriceInCents(subtotalInCents);

        // Apply discounts if enabled
        if (discountSettings.isEnabled()) {
            List<Discount> discounts = calculateDiscounts(orderRequest.orderLines(), subtotalInCents);
            order.setDiscounts(discounts);

            // Apply discount to get total price
            double totalDiscount = calculateTotalDiscount(discounts, subtotalInCents);
            order.setTotalPriceInCents(subtotalInCents - totalDiscount);
        } else {
            order.setDiscounts(List.of());
            order.setTotalPriceInCents(subtotalInCents);
        }

        order.setCurrency("EUR");
        return order;
    }

    public double calculateSubtotalInCents(List<OrderLine> orderLines) {
        if (CollectionUtils.isEmpty(orderLines)) {
            return 0.0;
        }

        return orderLines.stream()
                .mapToDouble(OrderLine::priceInCents)
                .sum();
    }

    public double calculateTotalDiscount(List<Discount> discounts, double subtotalInCents) {
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

    public List<Discount> calculateDiscounts(List<OrderLine> lines, double subtotalInCents) {
        if (CollectionUtils.isEmpty(lines)) {
            return List.of();
        }

        List<Discount> possibleDiscounts = new ArrayList<>();

        // 1. If the total cost of the cart is more than 12 euros, there should be a 25% discount.
        quarterDiscountCalculation(subtotalInCents, possibleDiscounts);

        // 2. If there are 3 or more drinks in the cart, the one with the lowest amount should be free.
        freeItemAfterThreeDiscountCalculation(lines, possibleDiscounts);

        // 3. If eligible for both promotions, use the one with the lowest cart amount (highest discount value)
        List<Discount> possibleDiscounts1 = calculatePossibleDiscountOnOrder(subtotalInCents, possibleDiscounts);
        if (possibleDiscounts1 != null) return possibleDiscounts1;

        return possibleDiscounts;
    }

    private static List<Discount> calculatePossibleDiscountOnOrder(double subtotalInCents, List<Discount> possibleDiscounts) {
        if (possibleDiscounts.size() > 1) {
            // Calculate the actual discount amount for each discount
            double percentageDiscountAmount = subtotalInCents * 0.25; // 25% discount
            double freeItemDiscountAmount = possibleDiscounts.stream()
                    .filter(d -> d.getAmountInCents() != null)
                    .mapToDouble(Discount::getAmountInCents)
                    .findFirst()
                    .orElse(0.0);

            // Keep only the discount that results in the lowest cart amount (highest discount value)
            if (percentageDiscountAmount >= freeItemDiscountAmount) {
                return possibleDiscounts.stream()
                        .filter(d -> d.getPercentage() != null)
                        .toList();
            } else {
                return possibleDiscounts.stream()
                        .filter(d -> d.getAmountInCents() != null)
                        .toList();
            }
        }
        return null;
    }

    private void freeItemAfterThreeDiscountCalculation(List<OrderLine> lines, List<Discount> possibleDiscounts) {
        List<OrderLine> coffeeLines = lines.stream()
                .filter(line -> line.coffees() != null && line.coffees().stream().anyMatch(item -> item instanceof Coffee))
                .toList();

        if (discountSettings.isFreeItemAfterThree() && coffeeLines.size() >= 3) {
            // Find the coffee with the lowest price
            OrderLine cheapestCoffeeLine = coffeeLines.stream()
                    .min(Comparator.comparing(OrderLine::priceInCents))
                    .orElse(null);

            if (cheapestCoffeeLine != null) {
                Discount discount = new Discount();
                discount.setName("Free drink for 3+ drinks in cart");
                discount.setAmountInCents(cheapestCoffeeLine.priceInCents());
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

    public String generateOrderNumber() {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RCS-" + timestamp;
    }
}
