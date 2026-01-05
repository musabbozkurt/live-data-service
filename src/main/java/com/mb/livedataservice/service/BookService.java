package com.mb.livedataservice.service;

import com.mb.livedataservice.data.model.Book;

import java.util.List;

public interface BookService {

    List<Book> findAll();

    Book findById(Long id);

    List<Book> findByExample(Book book);

    Book save(Book book);

    Book update(Long id, Book book);

    boolean deleteById(Long id);

    Book createBook(String title, Long authorId, Integer publishedYear);

    Book updateBook(Long id, String title, Long authorId, Integer publishedYear);
}
