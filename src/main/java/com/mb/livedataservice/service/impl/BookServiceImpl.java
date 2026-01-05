package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.data.model.Author;
import com.mb.livedataservice.data.model.Book;
import com.mb.livedataservice.data.repository.BookGraphQLRepository;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.service.AuthorService;
import com.mb.livedataservice.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookGraphQLRepository bookGraphQlRepository;
    private final AuthorService authorService;

    @Override
    public List<Book> findAll() {
        return bookGraphQlRepository.findAll();
    }

    @Override
    public Book findById(Long id) {
        return bookGraphQlRepository.findById(id).orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
    }

    @Override
    public List<Book> findByExample(Book book) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        return bookGraphQlRepository.findAll(Example.of(book, matcher));
    }

    @Override
    @Transactional
    public Book save(Book book) {
        return bookGraphQlRepository.save(book);
    }

    @Override
    @Transactional
    public Book update(Long id, Book book) {
        return bookGraphQlRepository.findById(id)
                .map(existingBook -> {
                    existingBook.setTitle(book.getTitle());
                    existingBook.setAuthor(book.getAuthor());
                    existingBook.setPublishedYear(book.getPublishedYear());
                    return bookGraphQlRepository.save(existingBook);
                })
                .orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (bookGraphQlRepository.existsById(id)) {
            bookGraphQlRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Book createBook(String title, Long authorId, Integer publishedYear) {
        Author author = authorService.findById(authorId);
        Book book = new Book(title, author, publishedYear);
        return bookGraphQlRepository.save(book);
    }

    @Override
    @Transactional
    public Book updateBook(Long id, String title, Long authorId, Integer publishedYear) {
        Book existingBook = bookGraphQlRepository.findById(id)
                .orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
        Author author = authorService.findById(authorId);
        existingBook.setTitle(title);
        existingBook.setAuthor(author);
        existingBook.setPublishedYear(publishedYear);
        return bookGraphQlRepository.save(existingBook);
    }
}
