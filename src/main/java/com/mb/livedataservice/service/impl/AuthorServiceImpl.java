package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.data.model.Author;
import com.mb.livedataservice.data.repository.AuthorGraphQLRepository;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorServiceImpl implements AuthorService {

    private final AuthorGraphQLRepository authorGraphQLRepository;

    @Override
    public List<Author> findAll() {
        return authorGraphQLRepository.findAll();
    }

    @Override
    public Author findById(Long id) {
        return authorGraphQLRepository.findById(id).orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
    }

    @Override
    public List<Author> findByExample(Author author) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        return authorGraphQLRepository.findAll(Example.of(author, matcher));
    }

    @Override
    @Transactional
    public Author save(Author author) {
        return authorGraphQLRepository.save(author);
    }

    @Override
    @Transactional
    public Author update(Long id, Author author) {
        return authorGraphQLRepository.findById(id)
                .map(existingAuthor -> {
                    existingAuthor.setName(author.getName());
                    existingAuthor.setCountry(author.getCountry());
                    return authorGraphQLRepository.save(existingAuthor);
                })
                .orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (authorGraphQLRepository.existsById(id)) {
            authorGraphQLRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
