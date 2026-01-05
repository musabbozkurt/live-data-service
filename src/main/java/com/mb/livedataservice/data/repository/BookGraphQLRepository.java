package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.Book;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.graphql.data.GraphQlRepository;

import java.util.List;
import java.util.Optional;

@GraphQlRepository
public interface BookGraphQLRepository extends JpaRepository<Book, Long>, QueryByExampleExecutor<Book> {

    @Override
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.author")
    List<Book> findAll();

    @Override
    @EntityGraph(attributePaths = {"author"})
    <S extends Book> List<S> findAll(Example<S> example);

    @Override
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.author WHERE b.id = :id")
    Optional<Book> findById(Long id);
}
