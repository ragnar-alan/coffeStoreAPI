package com.coffee.coffeestoreapi.service;

import com.coffee.coffeestoreapi.config.settings.DiscountSettings;
import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.exception.NoDrinkException;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
        var isThereAnyMissingDrinks = checkOrderLinesForDrinks(orderRequest.orderLines());
        if (isThereAnyMissingDrinks) {
            log.error("Order line do not contain drink.");
            throw new NoDrinkException("Order line do not contain drink.");
        }
        var order = new Order();
        order.setOrderNumber(generateOrderNumber());
        return populateOrder(orderRequest.orderer(), orderRequest.orderLines(), order, Optional.of(orderRequest.currency()));
    }

    public Order processChangedOrder(AdminOrderChangeRequest request, Order order) {
        return populateOrder(request.orderer(), request.orderLines(), order, Optional.empty());
    }

    /**
     * Calculates the subtotal price in cents for the given order lines.
     *
     * @param orderLines the list of order lines
     * @return the subtotal price in cents
     */
    protected int calculateSubtotalInCents(List<OrderLine> orderLines) {
        if (CollectionUtils.isEmpty(orderLines)) {
            return 0;
        }

        return orderLines.stream()
                .mapToInt(OrderLine::priceInCents)
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
    protected int calculateTotalDiscount(List<Discount> discounts, int subtotalInCents) {
        if (CollectionUtils.isEmpty(discounts)) {
            return 0;
        }

        // Find the discount that results in the lowest cart amount
        return discounts.stream()
                .mapToInt(discount -> {
                    if (discount.getPercentage() != null) {
                        return subtotalInCents * discount.getPercentage() / 100;
                    } else if (discount.getAmountInCents() != null) {
                        return discount.getAmountInCents();
                    }
                    return 0;
                })
                .max()
                .orElse(0);
    }

    /**
     * Determines the applicable discounts for the given order lines and subtotal.
     * Returns only the discount(s) that result in the lowest cart amount.
     *
     * @param lines the list of order lines
     * @param subtotalInCents the subtotal price in cents
     * @return the list of applicable discounts
     */
    protected List<Discount> calculateDiscounts(List<OrderLine> lines, int subtotalInCents) {
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
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "RCS-" + timestamp;
    }

    private Boolean checkOrderLinesForDrinks(List<OrderLine> orderLines) {
        return orderLines.stream()
                .anyMatch(orderLine -> orderLine.drink() == null);
    }

    private static List<Discount> calculatePossibleDiscountOnOrder(int subtotalInCents, List<Discount> possibleDiscounts) {
        // Calculate the actual discount amount for each discount
        int percentageDiscountAmount = (subtotalInCents * 25) / 100; // 25% discount
        int freeItemDiscountAmount = possibleDiscounts.stream()
                .filter(discount -> discount.getAmountInCents() != null)
                .mapToInt(Discount::getAmountInCents)
                .findFirst()
                .orElse(0);

        // Keep only the discount that results in the lowest cart amount (highest discount value)
        if (percentageDiscountAmount <= freeItemDiscountAmount) {
            return possibleDiscounts.stream()
                    .filter(discount -> discount.getAmountInCents() != null)
                    .toList();
        } else {
            return possibleDiscounts.stream()
                    .filter(discount -> discount.getPercentage() != null)
                    .toList();
        }
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

    private void quarterDiscountCalculation(int subtotalInCents, List<Discount> possibleDiscounts) {
        if (discountSettings.isTwentyFivePercent() && subtotalInCents > 1200) { // 12 euros in cents
            Discount discount = new Discount();
            discount.setName("25% off for orders over €12");
            discount.setPercentage(25);
            discount.setAmountInCents((subtotalInCents * 25) / 100);
            possibleDiscounts.add(discount);
        }
    }

    private Order populateOrder(String orderer, List<OrderLine> orderLines, Order order, Optional<Currency> currency) {
        order.setOrderer(orderer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderLines(orderLines);

        int subtotalInCents = calculateSubtotalInCents(orderLines);
        order.setSubTotalPriceInCents(subtotalInCents);

        if (discountSettings.isEnabled()) {
            List<Discount> discounts = calculateDiscounts(orderLines, subtotalInCents);
            order.setDiscounts(discounts);
            int totalDiscount = calculateTotalDiscount(discounts, subtotalInCents);
            order.setTotalPriceInCents(subtotalInCents - totalDiscount);
        } else {
            order.setDiscounts(List.of());
            order.setTotalPriceInCents(subtotalInCents);
        }

        if (order.getCurrency() == null && currency.isPresent()) {
            order.setCurrency(currency.get());
        } else {
            order.setCurrency(Currency.EUR);
        }
        return order;
    }
}
