package com.coffee.coffeestoreapi.repository;

import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByOrderNumberAndStatus(String orderNumber, OrderStatus status);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt ASC")
    List<Order> findAllDescendingCreationOrder();

    /**
     * Finds the most popular drink across all orders.
     * 
     * @return a Map containing the drink name and count
     */
    @Query(value = 
           "SELECT jsonb_extract_path_text(drink, 'name') as name, COUNT(*) as count " +
           "FROM orders, jsonb_array_elements(order_lines) as order_line, " +
           "jsonb_extract_path(order_line, 'drink') as drink " +
           "WHERE status != 'CANCELLED' " +
           "GROUP BY jsonb_extract_path_text(drink, 'name') " +
           "ORDER BY count DESC " +
           "LIMIT 1", nativeQuery = true)
    Map<String, Object> findMostPopularDrink();

    /**
     * Finds the most popular topping across all orders.
     * 
     * @return a Map containing the topping name and count
     */
    @Query(value = 
           "SELECT jsonb_extract_path_text(topping, 'name') as name, COUNT(*) as count " +
           "FROM orders, jsonb_array_elements(order_lines) as order_line, " +
           "jsonb_array_elements(jsonb_extract_path(order_line, 'toppings')) as topping " +
           "WHERE status != 'CANCELLED' " +
           "GROUP BY jsonb_extract_path_text(topping, 'name') " +
           "ORDER BY count DESC " +
           "LIMIT 1", nativeQuery = true)
    Map<String, Object> findMostPopularTopping();
}
