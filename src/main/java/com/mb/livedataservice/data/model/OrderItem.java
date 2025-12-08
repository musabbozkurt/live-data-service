package com.mb.livedataservice.data.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("order_items")
public record OrderItem(@Id
                        Long id,
                        Long orderId,
                        Long coffeeId,
                        Integer quantity,
                        BigDecimal price) {

    public OrderItem {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (coffeeId == null) {
            throw new IllegalArgumentException("Coffee ID cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
    }

    public OrderItem(Long orderId, Long coffeeId, Integer quantity, BigDecimal price) {
        this(null, orderId, coffeeId, quantity, price);
    }
}
