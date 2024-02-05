package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface TutorialRepository extends JpaRepository<Tutorial, Long>, QuerydslPredicateExecutor<Tutorial> {

    List<Tutorial> findByPublished(boolean published);

    List<Tutorial> findByTitleContaining(String title);
}
