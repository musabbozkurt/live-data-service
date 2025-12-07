package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.OrderItem;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrderItemRepository demonstrating Spring Data AOT for relationship queries.
 */
@Repository
public interface OrderItemRepository extends ListCrudRepository<OrderItem, Long> {

    /**
     * DEMONSTRATES: Foreign key relationship query.
     * <p>
     * AOT generates: SELECT * FROM order_items WHERE order_id = ?
     * <p>
     * Benefit: Relationship queries are optimized at build time with
     * proper index usage consideration.
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * DEMONSTRATES: JOIN query with sorting across tables.
     * <p>
     * AOT validates the JOIN between order_items and coffee tables,
     * ensuring foreign key relationships are correct.
     * <p>
     * Benefit: Cross-table queries are fully validated at compile time,
     * catching relationship errors before deployment.
     */
    @Query("""
            SELECT oi.* FROM order_items oi
            INNER JOIN coffee c ON oi.coffee_id = c.id
            WHERE oi.order_id = :orderId
            ORDER BY c.name
            """)
    List<OrderItem> findOrderItemsWithCoffeeDetails(@Param("orderId") Long orderId);
}
