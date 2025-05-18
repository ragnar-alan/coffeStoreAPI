package com.coffee.coffeestoreapi.repository;

import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByOrderNumberAndStatus(String orderNumber, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<Order> findAllByStatusDescendingCreationOrder(@Param("status") OrderStatus status);

}
