package com.coffee.coffeestoreapi.entity;

import com.coffee.coffeestoreapi.model.Currency;
import com.coffee.coffeestoreapi.model.Discount;
import com.coffee.coffeestoreapi.model.OrderLine;
import com.coffee.coffeestoreapi.model.OrderStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
@Entity
public class Order {

    @Id
    @GeneratedValue(generator = "hibernate_sequence")
    @SequenceGenerator(name = "hibernate_sequence", allocationSize = 1)
    private Long id;

    private String orderNumber;

    @Nullable
    private String orderer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Discount> discounts;

    private Double subTotalPriceInCents;

    private Double totalPriceInCents;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<OrderLine> orderLines;

    @CreatedDate
    private Timestamp createdAt;

    @UpdateTimestamp
    @Nullable
    private LocalDate updatedAt;

    private LocalDate processedAt;

    private LocalDate completedAt;

    private Timestamp canceledAt;

}
