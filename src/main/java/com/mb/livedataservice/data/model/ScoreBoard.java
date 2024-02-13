package com.mb.livedataservice.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "score_board")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE score_board SET deleted=true WHERE id=?")
public class ScoreBoard extends BaseEntity {

    @Column(nullable = false)
    @Audited(withModifiedFlag = true)
    private String homeTeamName;

    @Column(nullable = false)
    @Audited(withModifiedFlag = true)
    private String awayTeamName;

    @Column(nullable = false)
    @Audited(withModifiedFlag = true)
    private int homeTeamScore;

    @Column(nullable = false)
    @Audited(withModifiedFlag = true)
    private int awayTeamScore;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ScoreBoard that = (ScoreBoard) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}