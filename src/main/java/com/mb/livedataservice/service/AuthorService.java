package com.mb.livedataservice.service;

import com.mb.livedataservice.data.model.Author;

import java.util.List;

public interface AuthorService {

    List<Author> findAll();

    Author findById(Long id);

    List<Author> findByExample(Author author);

    Author save(Author author);

    Author update(Long id, Author author);

    boolean deleteById(Long id);
}
