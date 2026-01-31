package com.mb.livedataservice.data.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "email_template")
public class EmailTemplate {

    @Id
    @GeneratedValue(generator = "email_template_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "email_template_seq", sequenceName = "email_template_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

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
