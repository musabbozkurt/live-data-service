package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.data.model.Author;
import com.mb.livedataservice.data.model.Book;
import com.mb.livedataservice.data.repository.AuthorGraphQLRepository;
import com.mb.livedataservice.data.repository.BookGraphQLRepository;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestcontainersConfiguration.class)
class BookGraphQLControllerTest {

    private static final int FLYWAY_BOOK_COUNT = 24;

    private HttpGraphQlTester httpGraphQlTester;

    @Autowired
    private BookGraphQLRepository bookGraphQlRepository;

    @Autowired
    private AuthorGraphQLRepository authorGraphQLRepository;

    @LocalServerPort
    private int port;

    @BeforeAll
    void setUp() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:%d/graphql".formatted(port))
                .build();

        httpGraphQlTester = HttpGraphQlTester.create(client);
    }

    @Test
    void books_ShouldReturnAllBooks_WhenNoFilterProvided() {
        String query = """
                query {
                    books {
                        id
                        title
                        author {
                            id
                            name
                            country
                        }
                        publishedYear
                    }
                }
                """;

        var response = httpGraphQlTester.document(query)
                .execute();

        List<Book> books = response
                .path("data.books")
                .entityList(Book.class)
                .get();

        assertThat(books)
                .hasSizeGreaterThanOrEqualTo(FLYWAY_BOOK_COUNT)
                .allSatisfy(book -> {
                    assertThat(book.getId()).isNotNull().isPositive();
                    assertThat(book.getTitle()).isNotBlank();
                    assertThat(book.getPublishedYear()).isNotNull().isBetween(2000, 2030);
                });
        assertThat(books).extracting(Book::getTitle).doesNotContainNull();
        assertThat(books).extracting(Book::getId).doesNotHaveDuplicates();

        // Verify author data for first book
        response.path("data.books[0].author.id").hasValue();
        response.path("data.books[0].author.name").hasValue();
        response.path("data.books[0].author.country").hasValue();

        // Verify all books have authors with required fields
        List<Author> authors = response
                .path("data.books[*].author")
                .entityList(Author.class)
                .get();

        assertThat(authors)
                .hasSizeGreaterThanOrEqualTo(FLYWAY_BOOK_COUNT)
                .allSatisfy(author -> {
                    assertThat(author.getId()).isNotNull().isPositive();
                    assertThat(author.getName()).isNotBlank();
                    assertThat(author.getCountry()).isNotBlank();
                });
    }

    @Test
    void books_ShouldReturnFilteredBooks_WhenFilterProvided() {
        String query = """
                query($bookInput: BookInput!) {
                    books(book: $bookInput) {
                        id
                        title
                        author {
                            id
                            name
                            country
                        }
                        publishedYear
                    }
                }
                """;

        var response = httpGraphQlTester.document(query)
                .variable("bookInput", Map.of("title", "Spring in Action"))
                .execute();

        List<Book> books = response
                .path("data.books")
                .entityList(Book.class)
                .get();

        assertThat(books).hasSize(2)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Spring in Action", "Cloud Native Spring in Action");
        assertThat(books).allSatisfy(book -> {
            assertThat(book.getId()).isNotNull();
            assertThat(book.getTitle()).containsIgnoringCase("Spring in Action");
            assertThat(book.getPublishedYear()).isNotNull();
            assertThat(book.getPublishedYear()).isBetween(2000, 2030);
        });
        assertThat(books).extracting(Book::getPublishedYear)
                .containsExactlyInAnyOrder(2018, 2022);

        // Verify author data for filtered books
        List<Author> authors = response
                .path("data.books[*].author")
                .entityList(Author.class)
                .get();

        assertThat(authors)
                .hasSize(2)
                .allSatisfy(author -> {
                    assertThat(author.getId()).isNotNull().isPositive();
                    assertThat(author.getName()).isNotBlank();
                    assertThat(author.getCountry()).isNotBlank();
                });
        assertThat(authors).extracting(Author::getName)
                .containsExactlyInAnyOrder("Craig Walls", "Thomas Vitale");
    }

    @Test
    void book_ShouldReturnBook_WhenBookExists() {
        Book existingBook = bookGraphQlRepository.findAll().getFirst();

        String query = """
                query($id: ID!) {
                    book(id: $id) {
                        id
                        title
                        author {
                            id
                            name
                            country
                        }
                        publishedYear
                    }
                }
                """;

        var response = httpGraphQlTester.document(query)
                .variable("id", existingBook.getId().toString())
                .execute();

        Book book = response
                .path("data.book")
                .entity(Book.class)
                .get();

        assertThat(book.getId()).isEqualTo(existingBook.getId());
        assertThat(book.getTitle()).isEqualTo(existingBook.getTitle());
        assertThat(book.getPublishedYear()).isEqualTo(existingBook.getPublishedYear());
        assertThat(book.getId()).isNotNull().isPositive();
        assertThat(book.getTitle()).isNotBlank();

        // Verify author data
        Author author = response
                .path("data.book.author")
                .entity(Author.class)
                .get();

        assertThat(author.getId()).isNotNull().isPositive();
        assertThat(author.getName()).isNotBlank();
        assertThat(author.getCountry()).isNotBlank();
        assertThat(author.getId()).isEqualTo(existingBook.getAuthor().getId());
        assertThat(author.getName()).isEqualTo(existingBook.getAuthor().getName());
    }

    @Test
    void createBook_ShouldCreateAndReturnBook() {
        Author author = authorGraphQLRepository.findAll().getFirst();

        String mutation = """
                mutation($title: String!, $authorId: ID!, $publishedYear: Int!) {
                    createBook(title: $title, authorId: $authorId, publishedYear: $publishedYear) {
                        id
                        title
                        author {
                            id
                            name
                            country
                        }
                        publishedYear
                    }
                }
                """;

        var response = httpGraphQlTester.document(mutation)
                .variable("title", "Test Book")
                .variable("authorId", author.getId().toString())
                .variable("publishedYear", 2024)
                .execute();

        Book createdBook = response
                .path("data.createBook")
                .entity(Book.class)
                .get();

        assertThat(createdBook.getId()).isNotNull().isPositive();
        assertThat(createdBook.getTitle()).isEqualTo("Test Book");
        assertThat(createdBook.getPublishedYear()).isEqualTo(2024);
        assertThat(createdBook.getTitle()).isNotBlank();
        assertThat(createdBook.getPublishedYear()).isBetween(2000, 2030);

        // Verify author data in response
        Author responseAuthor = response
                .path("data.createBook.author")
                .entity(Author.class)
                .get();

        assertThat(responseAuthor.getId()).isEqualTo(author.getId());
        assertThat(responseAuthor.getName()).isEqualTo(author.getName());
        assertThat(responseAuthor.getCountry()).isEqualTo(author.getCountry());
        assertThat(responseAuthor.getId()).isNotNull().isPositive();
        assertThat(responseAuthor.getName()).isNotBlank();
        assertThat(responseAuthor.getCountry()).isNotBlank();

        // Verify book was persisted with correct author
        assertThat(bookGraphQlRepository.findById(createdBook.getId())).isPresent();
        Book dbBook = bookGraphQlRepository.findById(createdBook.getId()).orElseThrow();
        assertThat(dbBook.getAuthor().getId()).isEqualTo(author.getId());
    }

    @Test
    void updateBook_ShouldUpdateAndReturnBook_WhenBookExists() {
        Book existingBook = bookGraphQlRepository.findAll().getFirst();
        Author newAuthor = authorGraphQLRepository.findAll().getLast();

        String mutation = """
                mutation($id: ID!, $title: String!, $authorId: ID!, $publishedYear: Int!) {
                    updateBook(id: $id, title: $title, authorId: $authorId, publishedYear: $publishedYear) {
                        id
                        title
                        author {
                            id
                            name
                            country
                        }
                        publishedYear
                    }
                }
                """;

        var response = httpGraphQlTester.document(mutation)
                .variable("id", existingBook.getId().toString())
                .variable("title", "Updated Title")
                .variable("authorId", newAuthor.getId().toString())
                .variable("publishedYear", 2025)
                .execute();

        Book updatedBook = response
                .path("data.updateBook")
                .entity(Book.class)
                .get();

        assertThat(updatedBook.getId()).isEqualTo(existingBook.getId());
        assertThat(updatedBook.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedBook.getPublishedYear()).isEqualTo(2025);
        assertThat(updatedBook.getId()).isNotNull().isPositive();
        assertThat(updatedBook.getTitle()).isNotBlank();
        assertThat(updatedBook.getTitle()).isNotEqualTo(existingBook.getTitle());

        // Verify author data in response
        Author responseAuthor = response
                .path("data.updateBook.author")
                .entity(Author.class)
                .get();

        assertThat(responseAuthor.getId()).isEqualTo(newAuthor.getId());
        assertThat(responseAuthor.getName()).isEqualTo(newAuthor.getName());
        assertThat(responseAuthor.getCountry()).isEqualTo(newAuthor.getCountry());
        assertThat(responseAuthor.getId()).isNotNull().isPositive();
        assertThat(responseAuthor.getName()).isNotBlank();
        assertThat(responseAuthor.getCountry()).isNotBlank();

        // Verify book was updated in database with correct author
        Book dbBook = bookGraphQlRepository.findById(updatedBook.getId()).orElseThrow();
        assertThat(dbBook.getTitle()).isEqualTo("Updated Title");
        assertThat(dbBook.getPublishedYear()).isEqualTo(2025);
        assertThat(dbBook.getAuthor().getId()).isEqualTo(newAuthor.getId());
    }

    @Test
    void deleteBook_ShouldReturnTrue_WhenBookExists() {
        Author author = authorGraphQLRepository.findAll().getFirst();
        Book bookToDelete = bookGraphQlRepository.save(new Book("Delete Me", author, 2020));
        Long deletedBookId = bookToDelete.getId();

        String mutation = """
                mutation($id: ID!) {
                    deleteBook(id: $id)
                }
                """;

        Boolean result = httpGraphQlTester.document(mutation)
                .variable("id", bookToDelete.getId().toString())
                .execute()
                .path("data.deleteBook")
                .entity(Boolean.class)
                .get();

        assertThat(result).isTrue();
        assertThat(bookGraphQlRepository.findById(deletedBookId)).isEmpty();
        assertThat(bookGraphQlRepository.existsById(deletedBookId)).isFalse();
    }

    @Test
    void deleteBook_ShouldReturnFalse_WhenBookDoesNotExist() {
        long nonExistentId = 999999L;

        // Ensure book doesn't exist before test
        assertThat(bookGraphQlRepository.existsById(nonExistentId)).isFalse();

        String mutation = """
                mutation($id: ID!) {
                    deleteBook(id: $id)
                }
                """;

        Boolean result = httpGraphQlTester.document(mutation)
                .variable("id", "999999")
                .execute()
                .path("data.deleteBook")
                .entity(Boolean.class)
                .get();

        assertThat(result).isFalse();
    }
}
