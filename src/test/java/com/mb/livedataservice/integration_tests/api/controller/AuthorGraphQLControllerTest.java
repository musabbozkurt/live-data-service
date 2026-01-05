package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.data.model.Author;
import com.mb.livedataservice.data.model.Book;
import com.mb.livedataservice.data.repository.AuthorGraphQLRepository;
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
class AuthorGraphQLControllerTest {

    private static final int FLYWAY_AUTHOR_COUNT = 21;

    private HttpGraphQlTester httpGraphQlTester;

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
    void authors_ShouldReturnAllAuthors_WhenNoFilterProvided() {
        String query = """
                query {
                    authors {
                        id
                        name
                        country
                        books {
                            id
                            title
                            publishedYear
                        }
                    }
                }
                """;

        var response = httpGraphQlTester.document(query)
                .execute();

        List<Author> authors = response
                .path("data.authors")
                .entityList(Author.class)
                .get();

        assertThat(authors)
                .hasSizeGreaterThanOrEqualTo(FLYWAY_AUTHOR_COUNT)
                .allSatisfy(author -> {
                    assertThat(author.getId()).isNotNull().isPositive();
                    assertThat(author.getName()).isNotBlank();
                    assertThat(author.getCountry()).isNotBlank();
                });
        assertThat(authors).extracting(Author::getName).doesNotContainNull();
        assertThat(authors).extracting(Author::getId).doesNotHaveDuplicates();

        // Verify books data for first author
        response.path("data.authors[0].books").hasValue();

        // Verify at least one author has books
        List<Book> firstAuthorBooks = response
                .path("data.authors[0].books")
                .entityList(Book.class)
                .get();

        assertThat(firstAuthorBooks)
                .isNotEmpty()
                .allSatisfy(book -> {
                    assertThat(book.getId()).isNotNull().isPositive();
                    assertThat(book.getTitle()).isNotBlank();
                    assertThat(book.getPublishedYear()).isNotNull().isBetween(2000, 2030);
                });
    }

    @Test
    void authors_ShouldReturnFilteredAuthors_WhenFilterProvided() {
        String query = """
                query($authorInput: AuthorInput!) {
                    authors(author: $authorInput) {
                        id
                        name
                        country
                    }
                }
                """;

        List<Author> authors = httpGraphQlTester.document(query)
                .variable("authorInput", Map.of("country", "USA"))
                .execute()
                .path("data.authors")
                .entityList(Author.class)
                .get();

        assertThat(authors)
                .isNotEmpty()
                .allSatisfy(author -> {
                    assertThat(author.getCountry()).isEqualTo("USA");
                    assertThat(author.getId()).isNotNull().isPositive();
                    assertThat(author.getName()).isNotBlank();
                });
        assertThat(authors).extracting(Author::getId).doesNotHaveDuplicates();
        assertThat(authors).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void author_ShouldReturnAuthor_WhenAuthorExists() {
        Author existingAuthor = authorGraphQLRepository.findAll().getFirst();

        String query = """
                query($id: ID!) {
                    author(id: $id) {
                        id
                        name
                        country
                        books {
                            id
                            title
                            publishedYear
                        }
                    }
                }
                """;

        var response = httpGraphQlTester.document(query)
                .variable("id", existingAuthor.getId().toString())
                .execute();

        Author author = response
                .path("data.author")
                .entity(Author.class)
                .get();

        assertThat(author.getId()).isEqualTo(existingAuthor.getId());
        assertThat(author.getName()).isEqualTo(existingAuthor.getName());
        assertThat(author.getCountry()).isEqualTo(existingAuthor.getCountry());
        assertThat(author.getId()).isNotNull().isPositive();
        assertThat(author.getName()).isNotBlank();
        assertThat(author.getCountry()).isNotBlank();

        // Verify books data for this author
        response.path("data.author.books").hasValue();

        List<Book> authorBooks = response
                .path("data.author.books")
                .entityList(Book.class)
                .get();

        assertThat(authorBooks)
                .isNotEmpty()
                .allSatisfy(book -> {
                    assertThat(book.getId()).isNotNull().isPositive();
                    assertThat(book.getTitle()).isNotBlank();
                    assertThat(book.getPublishedYear()).isNotNull().isBetween(2000, 2030);
                });
        assertThat(authorBooks).extracting(Book::getId).doesNotHaveDuplicates();
    }

    @Test
    void createAuthor_ShouldCreateAndReturnAuthor() {
        String mutation = """
                mutation($name: String!, $country: String!) {
                    createAuthor(name: $name, country: $country) {
                        id
                        name
                        country
                    }
                }
                """;

        Author createdAuthor = httpGraphQlTester.document(mutation)
                .variable("name", "Test Author")
                .variable("country", "Test Country")
                .execute()
                .path("data.createAuthor")
                .entity(Author.class)
                .get();

        assertThat(createdAuthor.getId()).isNotNull().isPositive();
        assertThat(createdAuthor.getName()).isEqualTo("Test Author");
        assertThat(createdAuthor.getCountry()).isEqualTo("Test Country");
        assertThat(createdAuthor.getName()).isNotBlank();
        assertThat(createdAuthor.getCountry()).isNotBlank();

        // Verify author was persisted
        assertThat(authorGraphQLRepository.findById(createdAuthor.getId())).isPresent();
        Author dbAuthor = authorGraphQLRepository.findById(createdAuthor.getId()).orElseThrow();
        assertThat(dbAuthor.getName()).isEqualTo("Test Author");
        assertThat(dbAuthor.getCountry()).isEqualTo("Test Country");
    }

    @Test
    void updateAuthor_ShouldUpdateAndReturnAuthor_WhenAuthorExists() {
        Author existingAuthor = authorGraphQLRepository.findAll().getFirst();

        String mutation = """
                mutation($id: ID!, $name: String!, $country: String!) {
                    updateAuthor(id: $id, name: $name, country: $country) {
                        id
                        name
                        country
                    }
                }
                """;

        Author updatedAuthor = httpGraphQlTester.document(mutation)
                .variable("id", existingAuthor.getId().toString())
                .variable("name", "Updated Name")
                .variable("country", "Updated Country")
                .execute()
                .path("data.updateAuthor")
                .entity(Author.class)
                .get();

        assertThat(updatedAuthor.getId()).isEqualTo(existingAuthor.getId());
        assertThat(updatedAuthor.getName()).isEqualTo("Updated Name");
        assertThat(updatedAuthor.getCountry()).isEqualTo("Updated Country");
        assertThat(updatedAuthor.getId()).isNotNull().isPositive();
        assertThat(updatedAuthor.getName()).isNotBlank();
        assertThat(updatedAuthor.getCountry()).isNotBlank();
        assertThat(updatedAuthor.getName()).isNotEqualTo(existingAuthor.getName());

        // Verify author was updated in database
        Author dbAuthor = authorGraphQLRepository.findById(updatedAuthor.getId()).orElseThrow();
        assertThat(dbAuthor.getName()).isEqualTo("Updated Name");
        assertThat(dbAuthor.getCountry()).isEqualTo("Updated Country");
    }

    @Test
    void deleteAuthor_ShouldReturnTrue_WhenAuthorExists() {
        Author authorToDelete = authorGraphQLRepository.save(new Author("Delete Me", "Country"));
        Long deletedAuthorId = authorToDelete.getId();

        String mutation = """
                mutation($id: ID!) {
                    deleteAuthor(id: $id)
                }
                """;

        Boolean result = httpGraphQlTester.document(mutation)
                .variable("id", authorToDelete.getId().toString())
                .execute()
                .path("data.deleteAuthor")
                .entity(Boolean.class)
                .get();

        assertThat(result).isTrue();
        assertThat(authorGraphQLRepository.findById(deletedAuthorId)).isEmpty();
        assertThat(authorGraphQLRepository.existsById(deletedAuthorId)).isFalse();
    }

    @Test
    void deleteAuthor_ShouldReturnFalse_WhenAuthorDoesNotExist() {
        long nonExistentId = 999999L;

        // Ensure author doesn't exist before test
        assertThat(authorGraphQLRepository.existsById(nonExistentId)).isFalse();

        String mutation = """
                mutation($id: ID!) {
                    deleteAuthor(id: $id)
                }
                """;

        Boolean result = httpGraphQlTester.document(mutation)
                .variable("id", "999999")
                .execute()
                .path("data.deleteAuthor")
                .entity(Boolean.class)
                .get();

        assertThat(result).isFalse();
    }
}
