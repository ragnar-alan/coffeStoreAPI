package com.coffee.coffeestoreapi.service;

import com.coffee.coffeestoreapi.config.settings.DiscountSettings;
import com.coffee.coffeestoreapi.model.Discount;
import com.coffee.coffeestoreapi.model.Drink;
import com.coffee.coffeestoreapi.model.OrderLine;
import com.coffee.coffeestoreapi.model.OrderRequest;
import com.coffee.coffeestoreapi.model.Topping;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.coffee.coffeestoreapi.model.Currency.EUR;

public class BaseTest {

    // Test data for drinks
    protected static final Drink ESPRESSO = createDrink("Espresso", 250.0);
    protected static final Drink LATTE = createDrink("Latte", 350.0);
    protected static final Drink CAPPUCCINO = createDrink("Cappuccino", 300.0);
    protected static final Drink AMERICANO = createDrink("Americano", 280.0);

    // Test data for toppings
    protected static final Topping MILK = createTopping("Milk", 50.0);
    protected static final Topping SUGAR = createTopping("Sugar", 20.0);
    protected static final Topping CINNAMON = createTopping("Cinnamon", 30.0);

    // Method sources for parameterized tests

    // Test data for calculateSubtotalInCents method
    public static Stream<Arguments> subtotalTestCases() {
        return Stream.of(
            // Empty order lines
            Arguments.of(Collections.emptyList(), 0.0),

            // Single order line
            Arguments.of(List.of(
                new OrderLine(250.0, ESPRESSO, Collections.emptyList())
            ), 250.0),

            // Multiple order lines
            Arguments.of(List.of(
                new OrderLine(250.0, ESPRESSO, Collections.emptyList()),
                new OrderLine(350.0, LATTE, Collections.emptyList())
            ), 600.0),

            // Order lines with toppings
            Arguments.of(List.of(
                new OrderLine(300.0, ESPRESSO, List.of(MILK, SUGAR)),
                new OrderLine(400.0, LATTE, List.of(CINNAMON))
            ), 700.0)
        );
    }

    // Test data for calculateTotalDiscount method
    public static Stream<Arguments> discountTestCases() {
        return Stream.of(
            // No discounts
            Arguments.of(Collections.emptyList(), 1000.0, 0.0),

            // Percentage discount
            Arguments.of(List.of(createPercentageDiscount("25% off", 25.0)), 1000.0, 250.0),

            // Fixed amount discount
            Arguments.of(List.of(createAmountDiscount("$5 off", 500.0)), 1000.0, 500.0),

            // Multiple discounts (should take the highest)
            Arguments.of(List.of(
                createPercentageDiscount("10% off", 10.0),
                createAmountDiscount("$2 off", 200.0)
            ), 1000.0, 200.0),

            Arguments.of(List.of(
                createPercentageDiscount("30% off", 30.0),
                createAmountDiscount("$2 off", 200.0)
            ), 1000.0, 300.0)
        );
    }

    // Test data for calculateDiscounts method
    public static Stream<Arguments> discountCalculationTestCases() {
        return Stream.of(
            // Empty order lines
            Arguments.of(Collections.emptyList(), 0.0, Collections.emptyList()),

            // Order under 12 euros, less than 3 drinks
            Arguments.of(
                List.of(new OrderLine(500.0, ESPRESSO, Collections.emptyList())),
                500.0,
                Collections.emptyList()
            ),

            // Order over 12 euros (25% discount applies)
            Arguments.of(
                List.of(
                    new OrderLine(700.0, LATTE, Collections.emptyList()),
                    new OrderLine(600.0, CAPPUCCINO, Collections.emptyList())
                ),
                1300.0,
                List.of(createPercentageDiscount("25% off for orders over €12", 25.0))
            ),

            // Order with 3+ drinks (free cheapest drink applies)
            Arguments.of(
                List.of(
                    new OrderLine(300.0, ESPRESSO, Collections.emptyList()),
                    new OrderLine(350.0, LATTE, Collections.emptyList()),
                    new OrderLine(280.0, AMERICANO, Collections.emptyList())
                ),
                930.0,
                List.of(createAmountDiscount("Free drink for 3+ drink in cart", 280.0))
            ),

            // Order eligible for both discounts (returns both discounts)
            Arguments.of(
                List.of(
                    new OrderLine(500.0, ESPRESSO, Collections.emptyList()),
                    new OrderLine(500.0, LATTE, Collections.emptyList()),
                    new OrderLine(300.0, AMERICANO, Collections.emptyList())
                ),
                1300.0,
                List.of(
                    createPercentageDiscount("25% off for orders over €12", 25.0),
                    createAmountDiscount("Free drink for 3+ drink in cart", 300.0)
                )
            )
        );
    }

    // Test data for processOrder method
    public static Stream<Arguments> orderProcessingTestCases() {
        return Stream.of(
            // Basic order with no discounts
            Arguments.of(
                createOrderRequest("John Doe", List.of(
                    new OrderLine(300.0, ESPRESSO, Collections.emptyList())
                )),
                false, false, false,
                300.0, 300.0, 0
            ),

            // Order with 25% discount enabled
            Arguments.of(
                createOrderRequest("Jane Smith", List.of(
                    new OrderLine(700.0, LATTE, Collections.emptyList()),
                    new OrderLine(600.0, CAPPUCCINO, Collections.emptyList())
                )),
                true, true, false,
                1300.0, 975.0, 1
            ),

            // Order with free item discount enabled
            Arguments.of(
                createOrderRequest("Bob Johnson", List.of(
                    new OrderLine(300.0, ESPRESSO, Collections.emptyList()),
                    new OrderLine(350.0, LATTE, Collections.emptyList()),
                    new OrderLine(280.0, AMERICANO, Collections.emptyList())
                )),
                true, false, true,
                930.0, 650.0, 1
            ),

            // Order eligible for both discounts (returns both discounts)
            Arguments.of(
                createOrderRequest("Alice Brown", List.of(
                    new OrderLine(500.0, ESPRESSO, Collections.emptyList()),
                    new OrderLine(500.0, LATTE, Collections.emptyList()),
                    new OrderLine(300.0, AMERICANO, Collections.emptyList())
                )),
                true, true, true,
                1300.0, 975.0, 2
            )
        );
    }

    // Helper methods to create test objects
    private static Drink createDrink(String name, Double priceInCents) {
        Drink drink = new Drink();
        drink.setName(name);
        drink.setPriceInCents(priceInCents);
        return drink;
    }

    private static Topping createTopping(String name, Double priceInCents) {
        Topping topping = new Topping();
        topping.setName(name);
        topping.setPriceInCents(priceInCents);
        return topping;
    }

    protected static Discount createPercentageDiscount(String name, Double percentage) {
        Discount discount = new Discount();
        discount.setName(name);
        discount.setPercentage(percentage);
        return discount;
    }

    protected static Discount createAmountDiscount(String name, Double amountInCents) {
        Discount discount = new Discount();
        discount.setName(name);
        discount.setAmountInCents(amountInCents);
        return discount;
    }

    protected static OrderRequest createOrderRequest(String orderer, List<OrderLine> orderLines) {
        double totalPrice = orderLines.stream().mapToDouble(OrderLine::priceInCents).sum();
        return new OrderRequest((int) totalPrice, orderer, EUR, orderLines);
    }
}
