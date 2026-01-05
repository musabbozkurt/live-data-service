package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.Author;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.graphql.data.GraphQlRepository;

import java.util.List;
import java.util.Optional;

@GraphQlRepository
public interface AuthorGraphQLRepository extends JpaRepository<Author, Long>, QueryByExampleExecutor<Author> {

    @Override
    @Query("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.books")
    List<Author> findAll();

    @Override
    @EntityGraph(attributePaths = {"books"})
    <S extends Author> List<S> findAll(Example<S> example);

    @Override
    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
    Optional<Author> findById(Long id);
}
