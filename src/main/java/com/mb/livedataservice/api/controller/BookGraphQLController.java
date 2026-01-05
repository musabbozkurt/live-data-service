package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.data.model.Author;
import com.mb.livedataservice.data.model.Book;
import com.mb.livedataservice.service.BookService;
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
public class BookGraphQLController {

    private final BookService bookService;

    @QueryMapping
    public List<Book> books(@Nullable @Argument Book book) {
        if (book == null || book.isEmpty()) {
            return bookService.findAll();
        }
        return bookService.findByExample(book);
    }

    @QueryMapping
    public Book book(@Argument Long id) {
        return bookService.findById(id);
    }

    @SchemaMapping(typeName = "Book", field = "author")
    public Author author(Book book) {
        return book.getAuthor();
    }

    @MutationMapping
    public Book createBook(@Argument String title, @Argument Long authorId, @Argument Integer publishedYear) {
        return bookService.createBook(title, authorId, publishedYear);
    }

    @MutationMapping
    public Book updateBook(@Argument Long id, @Argument String title, @Argument Long authorId, @Argument Integer publishedYear) {
        return bookService.updateBook(id, title, authorId, publishedYear);
    }

    @MutationMapping
    public boolean deleteBook(@Argument Long id) {
        return bookService.deleteById(id);
    }
}
