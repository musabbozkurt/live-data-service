package com.mb.livedataservice.service;

import com.mb.livedataservice.data.filter.TutorialFilter;
import com.mb.livedataservice.data.model.Tutorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TutorialService {

    List<Tutorial> findByTitleContaining(String title);

    Tutorial findById(long id);

    Tutorial save(Tutorial tutorial);

    Tutorial update(long id, Tutorial tutorial);

    void deleteById(long id);

    void deleteAll();

    List<Tutorial> findByPublished(boolean b);

    Page<Tutorial> findAll(TutorialFilter filter, Pageable pageable);
}
