package com.coffee.coffeestoreapi.entity;

import com.coffee.store.model.Discount;
import com.coffee.store.model.OrderLine;
import com.coffee.store.model.OrderStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity(name = "orders")
public class Order {

    @Id
    @GeneratedValue(generator = "hibernate_sequence")
    @SequenceGenerator(name = "hibernate_sequence", allocationSize = 1)
    private Long id;

    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private List<Discount> discounts;
    private Double subTotalPriceInCents;
    private Double totalPriceInCents;

    private String currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<OrderLine> orderLines;

    @CreatedBy
    private LocalDate createdAt;

    @UpdateTimestamp
    @Nullable
    private LocalDate updatedAt;

    private LocalDate processedAt;

    private LocalDate completedAt;

    @SoftDelete
    private LocalDate canceledAt;

}
