package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByCodeAndActiveTrue(String code);

    Optional<EmailTemplate> findByCode(String code);

    boolean existsByCode(String code);
}
