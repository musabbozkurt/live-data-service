package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.Coffee;
import com.mb.livedataservice.enums.Size;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CoffeeRepository extends ListCrudRepository<Coffee, Long> {

    /**
     * DEMONSTRATES: Complex derived query method with multiple keywords.
     * <p>
     * AOT analyzes this method name at compile time and generates optimized SQL:
     * SELECT * FROM coffee WHERE UPPER(name) LIKE UPPER('%' || ? || '%')
     *
     */
    List<Coffee> findByNameContainingIgnoreCase(String name);

    /**
     * DEMONSTRATES: Multi-property query with comparison operators.
     * <p>
     * AOT generates: SELECT * FROM coffee WHERE size = ? AND price > ?
     * <p>
     * Benefit: Complex AND conditions are parsed and validated at build time.
     * Performance-wise, there's zero runtime overhead for method parsing.
     */
    List<Coffee> findBySizeAndPriceGreaterThan(Size size, BigDecimal price);

    /**
     * DEMONSTRATES: Custom SQL with compile-time validation.
     * <p>
     * AOT validates this SQL query syntax during compilation. A typo in the
     * SQL (e.g., "SELCT" instead of "SELECT") fails the build immediately.
     * <p>
     * Benefit: Catch SQL errors at compile time, not when a customer hits
     * this endpoint in production. Also validates parameter binding.
     */
    @Query("""
            SELECT * FROM coffee
            WHERE size = :size
            AND price <= :maxPrice
            ORDER BY price DESC
            """)
    List<Coffee> findAffordableCoffeesBySize(@Param("size") String size,
                                             @Param("maxPrice") BigDecimal maxPrice);
}
