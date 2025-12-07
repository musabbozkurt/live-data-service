package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.Order;
import com.mb.livedataservice.enums.OrderStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderRepository demonstrating Spring Data AOT capabilities with temporal queries.
 */
@Repository
public interface OrderRepository extends ListCrudRepository<Order, Long> {

    /**
     * DEMONSTRATES: Simple derived query method.
     * <p>
     * AOT generates: SELECT * FROM orders WHERE customer_name = ?
     * <p>
     * Benefit: Clean, readable code with zero runtime overhead. The query
     * is pre-generated at build time.
     */
    List<Order> findByCustomerName(String customerName);

    /**
     * DEMONSTRATES: Multi-property query with temporal comparison.
     * <p>
     * AOT generates: SELECT * FROM orders WHERE status = ? AND order_date > ?
     * <p>
     * Benefit: Complex date/time queries are validated at build time. Wrong
     * date types or field names fail compilation, preventing production bugs.
     */
    List<Order> findByStatusAndOrderDateAfter(OrderStatus status, LocalDateTime date);

    /**
     * DEMONSTRATES: Complex JOIN query with compile-time validation.
     * <p>
     * This query joins three tables. AOT validates:
     * - Table names exist (orders, order_items, coffee)
     * - Column references are correct (o.id, oi.order_id, etc.)
     * - JOIN conditions are properly formed
     * <p>
     * Benefit: Multi-table queries are fully validated at build time. A typo
     * in a table name or column reference fails the build, not at runtime.
     */
    @Query("""
            SELECT DISTINCT o.* FROM orders o
            INNER JOIN order_items oi ON o.id = oi.order_id
            INNER JOIN coffee c ON oi.coffee_id = c.id
            WHERE c.name = :coffeeName
            ORDER BY o.order_date DESC
            """)
    List<Order> findOrdersByCoffeeName(@Param("coffeeName") String coffeeName);
}
