package com.mb.livedataservice.data.model;

import com.mb.livedataservice.enums.EmailStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "email_event")
public class EmailEvent {

    @Id
    private UUID id;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "to_addresses")
    private String to;

    @Column(name = "cc_addresses")
    private String cc;

    @Column(name = "bcc_addresses")
    private String bcc;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailStatus status = EmailStatus.SENT;

    @Column(nullable = false)
    private int retryCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
