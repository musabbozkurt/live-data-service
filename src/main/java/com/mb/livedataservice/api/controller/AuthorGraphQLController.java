package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.data.model.Author;
import com.mb.livedataservice.data.model.Book;
import com.mb.livedataservice.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthorGraphQLController {

    private final AuthorService authorService;

    @QueryMapping
    public List<Author> authors(@Nullable @Argument Author author) {
        if (author == null || author.isEmpty()) {
            return authorService.findAll();
        }
        return authorService.findByExample(author);
    }

    @QueryMapping
    public Author author(@Argument Long id) {
        return authorService.findById(id);
    }

    @SchemaMapping(typeName = "Author", field = "books")
    public List<Book> books(Author author) {
        return author.getBooks();
    }

    @MutationMapping
    public Author createAuthor(@Argument String name, @Argument String country) {
        Author author = new Author(name, country);
        return authorService.save(author);
    }

    @MutationMapping
    public Author updateAuthor(@Argument Long id, @Argument String name, @Argument String country) {
        Author author = new Author(name, country);
        return authorService.update(id, author);
    }

    @MutationMapping
    public boolean deleteAuthor(@Argument Long id) {
        return authorService.deleteById(id);
    }
}
