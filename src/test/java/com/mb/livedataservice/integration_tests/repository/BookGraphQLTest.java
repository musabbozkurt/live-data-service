package com.mb.livedataservice.integration_tests.repository;

import com.mb.livedataservice.data.model.Author;
import com.mb.livedataservice.data.model.Book;
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
class BookGraphQLTest {

    private static final int FLYWAY_BOOK_COUNT = 24; // Number of books seeded by Flyway migration

    private HttpGraphQlTester httpGraphQlTester;

    @Autowired
    private BookGraphQLRepository bookGraphQlRepository;

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
    void findAll_ShouldReturnAllBooks_WhenBooksExist() {
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
                .hasSize(FLYWAY_BOOK_COUNT)
                .allSatisfy(book -> {
                    assertThat(book.getId()).isNotNull().isPositive();
                    assertThat(book.getTitle()).isNotBlank();
                    assertThat(book.getPublishedYear()).isNotNull().isBetween(2000, 2030);
                });
        assertThat(books).extracting(Book::getTitle).doesNotContainNull();
        assertThat(books).extracting(Book::getId).doesNotHaveDuplicates();

        // Verify author data for all books
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
    void findById_ShouldReturnBook_WhenBookExists() {
        Book savedBook = bookGraphQlRepository.findAll().getFirst();

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
                .variable("id", savedBook.getId().toString())
                .execute();

        Book book = response
                .path("data.book")
                .entity(Book.class)
                .get();

        assertThat(book.getId()).isEqualTo(savedBook.getId());
        assertThat(book.getTitle()).isEqualTo(savedBook.getTitle());
        assertThat(book.getPublishedYear()).isEqualTo(savedBook.getPublishedYear());
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
        assertThat(author.getId()).isEqualTo(savedBook.getAuthor().getId());
        assertThat(author.getName()).isEqualTo(savedBook.getAuthor().getName());
    }

    @Test
    void findByExample_ShouldReturnMatchingBook_WhenExactTitleProvided() {
        String document = """
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

        var response = httpGraphQlTester.document(document)
                .variable("bookInput", Map.of("title", "Spring in Action"))
                .execute();

        List<Book> books = response
                .path("data.books")
                .entityList(Book.class)
                .get();

        assertThat(books).hasSize(2)
                .allSatisfy(book -> {
                    assertThat(book.getId()).isNotNull().isPositive();
                    assertThat(book.getTitle()).containsIgnoringCase("Spring in Action");
                    assertThat(book.getPublishedYear()).isNotNull();
                });
        assertThat(books.getFirst().getTitle()).isEqualTo("Cloud Native Spring in Action");
        assertThat(books.getFirst().getPublishedYear()).isEqualTo(2022);
        assertThat(books.getLast().getTitle()).isEqualTo("Spring in Action");
        assertThat(books.getLast().getPublishedYear()).isEqualTo(2018);
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
    void findByExample_ShouldReturnMatchingBook_WhenPublishedYearProvided() {
        String document = """
                    query($bookInput: BookInput!) {
                        books(book: $bookInput) {
                            id
                            title
                            author {
                                id
                                name
                            }
                            publishedYear
                        }
                    }
                """;

        List<Book> books = httpGraphQlTester.document(document)
                .variable("bookInput", Map.of("publishedYear", 2022))
                .execute()
                .path("data.books")
                .entityList(Book.class)
                .get();

        assertThat(books)
                .isNotEmpty()
                .allSatisfy(book -> {
                    assertThat(book.getPublishedYear()).isEqualTo(2022);
                    assertThat(book.getId()).isNotNull().isPositive();
                    assertThat(book.getTitle()).isNotBlank();
                });
        assertThat(books).extracting(Book::getId).doesNotHaveDuplicates();
        assertThat(books).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void findByExample_ShouldReturnEmptyList_WhenNoMatchFound() {
        String document = """
                    query($bookInput: BookInput!) {
                        books(book: $bookInput) {
                            id
                            title
                            author {
                                id
                                name
                            }
                            publishedYear
                        }
                    }
                """;

        List<Book> books = httpGraphQlTester.document(document)
                .variable("bookInput", Map.of("title", "Non Existent Book"))
                .execute()
                .path("data.books")
                .entityList(Book.class)
                .get();

        assertThat(books).isEmpty();
    }
}
