package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.EmailEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailEventRepository extends JpaRepository<EmailEvent, UUID> {

}
