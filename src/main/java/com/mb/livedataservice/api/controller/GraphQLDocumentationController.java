package com.mb.livedataservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Controller to document GraphQL APIs in Swagger UI.
 * The actual GraphQL endpoint is at /graphql and GraphiQL is at /graphiql
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/graphql")
@Tag(name = "GraphQL API Documentation", description = """
        GraphQL API documentation for the Live Data Service.
        
        **GraphQL Endpoint:** POST /graphql
        
        **GraphiQL UI:** GET /graphiql
        
        This controller provides documentation for GraphQL operations available in this service.
        To execute GraphQL queries, use the /graphql endpoint with a POST request or use GraphiQL at /graphiql.
        """)
public class GraphQLDocumentationController {

    private static final String QUERY = "query";
    private static final String VARIABLES = "variables";
    private static final String COUNTRY = "country";

    @GetMapping("/schema")
    @Operation(
            summary = "Get GraphQL Schema",
            description = "Returns the GraphQL schema definition (SDL) for this service"
    )
    @ApiResponse(
            responseCode = "200",
            description = "GraphQL Schema",
            content = @Content(mediaType = "text/plain")
    )
    public ResponseEntity<String> getGraphQLSchema() throws IOException {
        ClassPathResource resource = new ClassPathResource("graphql/schema.graphqls");
        String schema = resource.getContentAsString(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(schema);
    }

    @GetMapping("/info")
    @Operation(
            summary = "GraphQL API Information",
            description = """
                    Provides information about available GraphQL queries and mutations.
                    
                    ## Queries
                    
                    ### Authors
                    - `authors(author: AuthorInput): [Author!]!` - Get all authors or filter by criteria
                    - `author(id: ID!): Author` - Get a single author by ID
                    
                    ### Books
                    - `books(book: BookInput): [Book!]!` - Get all books or filter by criteria
                    - `book(id: ID!): Book` - Get a single book by ID
                    
                    ## Mutations
                    
                    ### Author Mutations
                    - `createAuthor(name: String!, country: String!): Author!` - Create a new author
                    - `updateAuthor(id: ID!, name: String!, country: String!): Author!` - Update an existing author
                    - `deleteAuthor(id: ID!): Boolean!` - Delete an author
                    
                    ### Book Mutations
                    - `createBook(title: String!, authorId: ID!, publishedYear: Int!): Book!` - Create a new book
                    - `updateBook(id: ID!, title: String!, authorId: ID!, publishedYear: Int!): Book!` - Update an existing book
                    - `deleteBook(id: ID!): Boolean!` - Delete a book
                    
                    ## Example Queries
                    
                    ### Get all books with authors
                    ```graphql
                    query {
                        books {
                            id
                            title
                            publishedYear
                            author {
                                id
                                name
                                country
                            }
                        }
                    }
                    ```
                    
                    ### Get all authors with books
                    ```graphql
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
                    ```
                    
                    ### Create a new author
                    ```graphql
                    mutation {
                        createAuthor(name: "John Doe", country: "USA") {
                            id
                            name
                            country
                        }
                    }
                    ```
                    """
    )
    public ResponseEntity<Map<String, Object>> getGraphQLInfo() {
        return ResponseEntity.ok(Map.of(
                "endpoint", "/graphql",
                "graphiql", "/graphiql",
                "documentation", "See the operation description for detailed API documentation",
                "queries", Map.of(
                        "authors", "Get all authors or filter by criteria",
                        "author", "Get a single author by ID",
                        "books", "Get all books or filter by criteria",
                        "book", "Get a single book by ID"
                ),
                "mutations", Map.of(
                        "createAuthor", "Create a new author",
                        "updateAuthor", "Update an existing author",
                        "deleteAuthor", "Delete an author",
                        "createBook", "Create a new book",
                        "updateBook", "Update an existing book",
                        "deleteBook", "Delete a book"
                )
        ));
    }

    @GetMapping("/examples/queries")
    @Operation(
            summary = "GraphQL Query Examples",
            description = "Returns example GraphQL queries that can be used with the /graphql endpoint"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Example queries",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Get All Books",
                                    value = """
                                            {
                                                "query": "query { books { id title publishedYear author { id name country } } }"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Get Book by ID",
                                    value = """
                                            {
                                                "query": "query($id: ID!) { book(id: $id) { id title publishedYear author { id name country } } }",
                                                "variables": { "id": "1" }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Get All Authors",
                                    value = """
                                            {
                                                "query": "query { authors { id name country books { id title publishedYear } } }"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Filter Authors by Country",
                                    value = """
                                            {
                                                "query": "query($author: AuthorInput!) { authors(author: $author) { id name country } }",
                                                "variables": { "author": { "country": "USA" } }
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<Map<String, Object>> getQueryExamples() {
        return ResponseEntity.ok(Map.of(
                "getAllBooks", Map.of(
                        QUERY, "query { books { id title publishedYear author { id name country } } }"
                ),
                "getBookById", Map.of(
                        QUERY, "query($id: ID!) { book(id: $id) { id title publishedYear author { id name country } } }",
                        VARIABLES, Map.of("id", "1")
                ),
                "getAllAuthors", Map.of(
                        QUERY, "query { authors { id name country books { id title publishedYear } } }"
                ),
                "filterAuthorsByCountry", Map.of(
                        QUERY, "query($author: AuthorInput!) { authors(author: $author) { id name country } }",
                        VARIABLES, Map.of("author", Map.of(COUNTRY, "USA"))
                )
        ));
    }

    @GetMapping("/examples/mutations")
    @Operation(
            summary = "GraphQL Mutation Examples",
            description = "Returns example GraphQL mutations that can be used with the /graphql endpoint"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Example mutations",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Create Author",
                                    value = """
                                            {
                                                "query": "mutation($name: String!, $country: String!) { createAuthor(name: $name, country: $country) { id name country } }",
                                                "variables": { "name": "John Doe", "country": "USA" }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Create Book",
                                    value = """
                                            {
                                                "query": "mutation($title: String!, $authorId: ID!, $publishedYear: Int!) { createBook(title: $title, authorId: $authorId, publishedYear: $publishedYear) { id title publishedYear author { id name } } }",
                                                "variables": { "title": "New Book", "authorId": "1", "publishedYear": 2024 }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Update Author",
                                    value = """
                                            {
                                                "query": "mutation($id: ID!, $name: String!, $country: String!) { updateAuthor(id: $id, name: $name, country: $country) { id name country } }",
                                                "variables": { "id": "1", "name": "Jane Doe", "country": "UK" }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Delete Book",
                                    value = """
                                            {
                                                "query": "mutation($id: ID!) { deleteBook(id: $id) }",
                                                "variables": { "id": "1" }
                                            }
                                            """
                            )
                    }
            )
    )
    public ResponseEntity<Map<String, Object>> getMutationExamples() {
        return ResponseEntity.ok(Map.of(
                "createAuthor", Map.of(
                        QUERY, "mutation($name: String!, $country: String!) { createAuthor(name: $name, country: $country) { id name country } }",
                        VARIABLES, Map.of("name", "John Doe", COUNTRY, "USA")
                ),
                "createBook", Map.of(
                        QUERY, "mutation($title: String!, $authorId: ID!, $publishedYear: Int!) { createBook(title: $title, authorId: $authorId, publishedYear: $publishedYear) { id title publishedYear author { id name } } }",
                        VARIABLES, Map.of("title", "New Book", "authorId", "1", "publishedYear", 2024)
                ),
                "updateAuthor", Map.of(
                        QUERY, "mutation($id: ID!, $name: String!, $country: String!) { updateAuthor(id: $id, name: $name, country: $country) { id name country } }",
                        VARIABLES, Map.of("id", "1", "name", "Jane Doe", COUNTRY, "UK")
                ),
                "deleteBook", Map.of(
                        QUERY, "mutation($id: ID!) { deleteBook(id: $id) }",
                        VARIABLES, Map.of("id", "1")
                )
        ));
    }
}
