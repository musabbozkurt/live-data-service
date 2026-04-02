package com.mb.livedataservice.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@Table(
        name = "shelf_entry",
        uniqueConstraints = {
                @UniqueConstraint(name = "udx_shelf_entry_01", columnNames = {"product_id", "start_date"})
        }
)
public class ShelfEntry {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_shelf_entry")
    @SequenceGenerator(name = "seq_shelf_entry", sequenceName = "seq_shelf_entry", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private boolean active;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ShelfEntry that = (ShelfEntry) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hp
                ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
