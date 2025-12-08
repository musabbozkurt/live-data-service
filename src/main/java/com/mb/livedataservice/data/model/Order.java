package com.mb.livedataservice.data.model;

import com.mb.livedataservice.enums.OrderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("orders")
public record Order(@Id
                    Long id,
                    Long customerId,
                    String customerName,
                    LocalDateTime orderDate,
                    BigDecimal totalAmount,
                    OrderStatus status) {

    public Order {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be blank");
        }
        if (orderDate == null) {
            throw new IllegalArgumentException("Order date cannot be null");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative");
        }
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }
    }

    public Order(Long customerId, String customerName, LocalDateTime orderDate, BigDecimal totalAmount, OrderStatus status) {
        this(null, customerId, customerName, orderDate, totalAmount, status);
    }
}
