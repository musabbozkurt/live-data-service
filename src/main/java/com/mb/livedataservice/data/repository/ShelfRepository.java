package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.ShelfEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShelfRepository extends JpaRepository<ShelfEntry, Long> {

    /**
     * Returns the record with the latest start_date for the given product.
     */
    @Query("""
            SELECT s FROM ShelfEntry s
            WHERE s.productId = :productId
            AND s.startDate = (
                SELECT MAX(s2.startDate) FROM ShelfEntry s2
                WHERE s2.productId = :productId
            )
            """)
    Optional<ShelfEntry> findByMaxStartDateAndProductId(Long productId);

    List<ShelfEntry> findAllByProductIdIn(Set<Long> productIds);

    @Modifying
    @Query(value = """
            INSERT INTO shelf_entry (id, product_id, start_date, active)
            VALUES (nextval('seq_shelf_entry'), :productId, :startDate, :active)
            ON CONFLICT (product_id, start_date)
            DO UPDATE SET active = EXCLUDED.active
            """, nativeQuery = true)
    void insertOrUpdateOnConflict(Long productId, LocalDate startDate, boolean active);
}
