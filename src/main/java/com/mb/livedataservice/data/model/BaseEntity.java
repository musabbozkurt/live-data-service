package com.mb.livedataservice.data.model;

import com.mb.livedataservice.util.LiveDataConstants;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@MappedSuperclass
@Audited(withModifiedFlag = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = LiveDataConstants.DEFAULT_ID_GENERATOR_NAME)
    private Long id;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private OffsetDateTime createdDateTime;

    @Column(nullable = false)
    private OffsetDateTime modifiedDateTime;

    @PrePersist
    protected void onPrePersist() {
        this.createdDateTime = OffsetDateTime.now();
        this.modifiedDateTime = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.modifiedDateTime = OffsetDateTime.now();
    }
}