package com.mb.livedataservice.integration_tests.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.JoinTypeRelation;
import org.springframework.data.elasticsearch.annotations.JoinTypeRelations;
import org.springframework.data.elasticsearch.annotations.Routing;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.join.JoinField;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.elasticsearch.client.elc.Queries.matchAllQueryAsQuery;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestcontainersConfiguration.class)
class StatementRepositoryIntegrationTest {

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private static Statement savedWeather;

    @BeforeAll
    void setUp() {
        savedWeather = statementRepository.save(
                Statement.builder()
                        .id("1")
                        .text("How is the weather?")
                        .relation(new JoinField<>("question"))
                        .build());

        Statement sunnyAnswer = statementRepository.save(
                Statement.builder()
                        .id("2")
                        .text("sunny")
                        .relation(new JoinField<>("answer", savedWeather.getId()))
                        .build());

        statementRepository.save(
                Statement.builder()
                        .id("3")
                        .text("rainy")
                        .relation(new JoinField<>("answer", savedWeather.getId()))
                        .build());

        statementRepository.save(
                Statement.builder()
                        .id("4")
                        .text("I don't like the rain")
                        .relation(new JoinField<>("comment", savedWeather.getId()))
                        .build());

        statementRepository.save(
                Statement.builder()
                        .id("5")
                        .text("+1 for the sun")
                        .routing(savedWeather.getId())
                        .relation(new JoinField<>("vote", sunnyAnswer.getId()))
                        .build());
    }

    @AfterAll
    void tearDown() {
        long countBefore = statementRepository.count();
        assertEquals(9, countBefore, "There should be 9 documents after the init");

        statementRepository.deleteAll();

        long countAfter = statementRepository.count();
        assertEquals(0, countAfter, "The statementRepository should be empty after deleteAll");
    }

    @Test
    void testSaveAndFindStatement() {
        Statement statement = Statement.builder()
                .id("1")
                .text("How is the weather?")
                .relation(new JoinField<>("question"))
                .routing("testRouting")
                .build();

        Statement savedStatement = statementRepository.save(statement);

        Optional<Statement> foundStatement = statementRepository.findById(savedStatement.getId());
        assertTrue(foundStatement.isPresent());
        assertEquals("How is the weather?", foundStatement.get().getText());
        assertEquals("testRouting", foundStatement.get().getRouting());
    }

    @Test
    void testSaveMultipleStatements() {
        Statement statement1 = Statement.builder()
                .id("6")
                .text("First test statement")
                .routing("firstRouting")
                .build();

        Statement statement2 = Statement.builder()
                .id("7")
                .text("Second test statement")
                .routing("secondRouting")
                .build();

        statementRepository.save(statement1);
        statementRepository.save(statement2);

        assertTrue(statementRepository.findById("6").isPresent());
        assertTrue(statementRepository.findById("7").isPresent());
    }

    @Test
    void testSearchStatements() {
        Statement statement = Statement.builder()
                .id("8")
                .text("I don't like the rain")
                .routing("searchRouting")
                .build();

        statementRepository.save(statement);

        assertEquals(2, elasticsearchOperations.count(new CriteriaQuery(Criteria.where("text").is("I don't like the rain")), Statement.class));
    }

    @Test
    void testSaveStatements() {
        Statement weather = statementRepository.findById("1").orElse(null);
        assertNotNull(weather, "The 'How is the weather?' question should be saved");
        assertEquals("How is the weather?", weather.getText());
        assertEquals("question", weather.getRelation().getName());

        Statement sunnyAnswer = statementRepository.findById("2").orElse(null);
        assertNotNull(sunnyAnswer, "The 'sunny' answer should be saved");
        assertEquals("sunny", sunnyAnswer.getText());
        assertEquals("answer", sunnyAnswer.getRelation().getName());

        Statement rainyAnswer = statementRepository.findById("3").orElse(null);
        assertNotNull(rainyAnswer, "The 'rainy' answer should be saved");
        assertEquals("rainy", rainyAnswer.getText());
        assertEquals("answer", rainyAnswer.getRelation().getName());

        Statement comment = statementRepository.findById("4").orElse(null);
        assertNotNull(comment, "The 'I don't like the rain' comment should be saved");
        assertEquals("I don't like the rain", comment.getText());
        assertEquals("comment", comment.getRelation().getName());

        Statement vote = statementRepository.findById("5").orElse(null);
        assertNotNull(vote, "The '+1 for the sun' vote should be saved");
        assertEquals("+1 for the sun", vote.getText());
        assertEquals("vote", vote.getRelation().getName());
    }

    @Test
    void testStatementRelations() {
        savedWeather = statementRepository.findById("1").orElse(null);
        assertNotNull(savedWeather, "The 'How is the weather?' question should be saved");

        Statement sunnyAnswer = statementRepository.findById("2").orElse(null);
        Statement rainyAnswer = statementRepository.findById("3").orElse(null);

        assertNotNull(sunnyAnswer, "The 'sunny' answer should be saved");
        assertEquals("answer", sunnyAnswer.getRelation().getName());
        assertEquals(savedWeather.getId(), sunnyAnswer.getRelation().getParent());

        assertNotNull(rainyAnswer, "The 'rainy' answer should be saved");
        assertEquals("answer", rainyAnswer.getRelation().getName());
        assertEquals(savedWeather.getId(), rainyAnswer.getRelation().getParent());

        Statement comment = statementRepository.findById("4").orElse(null);
        assertNotNull(comment, "The 'I don't like the rain' comment should be saved");
        assertEquals("comment", comment.getRelation().getName());
        assertEquals(savedWeather.getId(), comment.getRelation().getParent());

        Statement vote = statementRepository.findById("5").orElse(null);
        assertNotNull(vote, "The '+1 for the sun' vote should be saved");
        assertEquals("vote", vote.getRelation().getName());
        assertEquals(sunnyAnswer.getId(), vote.getRelation().getParent());
    }

    @Test
    void testFindStatementsByRouting() {
        Statement vote = statementRepository.findById("5").orElse(null);
        assertNotNull(vote, "The vote statement should be found using routing");
        assertEquals("+1 for the sun", vote.getText());
    }

    @Test
    void testSearchStatementsByText() {
        long count = elasticsearchOperations.count(CriteriaQuery.builder(Criteria.where("text").is("sunny")).build(), Statement.class);
        assertEquals(1, count, "There should be 1 statement with the text 'sunny'");

        count = elasticsearchOperations.count(CriteriaQuery.builder(Criteria.where("text").is("How is the weather?")).build(), Statement.class);
        assertEquals(1, count, "There should be 1 statement with the text 'How is the weather?'");
    }

    @Test
    void testSearchStatementsByRelation() {
        long count = elasticsearchOperations.count(CriteriaQuery.builder(Criteria.where("relation").is("question")).build(), Statement.class);
        assertEquals(1, count, "There should be 1 statement with relation 'question'");

        count = elasticsearchOperations.count(CriteriaQuery.builder(Criteria.where("relation").is("answer")).build(), Statement.class);
        assertEquals(2, count, "There should be 2 statements with relation 'answer'");
    }

    @Test
    void testDeleteStatements() {
        Statement statementToDelete = statementRepository.findById("8").orElse(null);
        assertNotNull(statementToDelete, "The statement to delete should exist");

        statementRepository.delete(statementToDelete);
        Optional<Statement> deletedStatement = statementRepository.findById("8");
        assertTrue(deletedStatement.isEmpty(), "The statement should be deleted");
    }

    @Test
    void testHasVotes_ShouldReturnTrue() {
        long count = hasVotes().stream().count();
        assertEquals(1, count, "There should be 1 vote");
    }

    @Test
    void testSaveXyzDocument() {
        // Save parent statement
        Statement parentStatement = Statement.builder()
                .id("customChildIndex-parent")
                .text("Parent Statement")
                .relation(new JoinField<>("question"))
                .build();
        statementRepository.save(parentStatement);

        // Create approval info objects
        ApprovalInfo quality = ApprovalInfo.builder()
                .departmentName("Quality")
                .description("Quality Department")
                .isActive(true)
                .build();

        ApprovalInfo supply = ApprovalInfo.builder()
                .departmentName("Supply")
                .description("Supply Department")
                .isActive(false)
                .build();

        ApprovalInfo merchandising = ApprovalInfo.builder()
                .departmentName("Merchandising")
                .description("Merchandising Department")
                .isActive(false)
                .build();

        // Save custom child index document with approval departments
        CustomChildIndex customChildIndex = CustomChildIndex.builder()
                .id("customChildIndex-child")
                .customChildData("Custom Child Data")
                .routing(parentStatement.getId())
                .relation(new JoinField<>("customChildIndex", parentStatement.getId()))
                .approvalInfos(List.of(quality, supply))
                .approvalInfoList(List.of(supply, merchandising))
                .build();

        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(customChildIndex.getId())
                .withRouting(parentStatement.getId())
                .withObject(customChildIndex)
                .build();

        elasticsearchOperations.index(indexQuery, IndexCoordinates.of("statements"));
        elasticsearchOperations.indexOps(Statement.class).refresh();

        // Verify parent-child relationship
        SearchHits<CustomChildIndex> childResults = findChildDocument(parentStatement.getId());
        CustomChildIndex foundChild = childResults.getSearchHit(0).getContent();

        // Test approval departments
        assertEquals(2, foundChild.getApprovalInfos().size());

        ApprovalInfo foundQuality = foundChild.getApprovalInfos().getFirst();
        assertEquals("Quality", foundQuality.getDepartmentName());
        assertEquals("Quality Department", foundQuality.getDescription());
        assertTrue(foundQuality.isActive());

        // Test waiting departments
        assertEquals(2, foundChild.getApprovalInfoList().size());

        ApprovalInfo foundSupply = foundChild.getApprovalInfoList().getFirst();
        assertEquals("Supply", foundSupply.getDepartmentName());
        assertFalse(foundSupply.isActive());

        ApprovalInfo foundMerchandising = foundChild.getApprovalInfoList().get(1);
        assertEquals("Merchandising", foundMerchandising.getDepartmentName());
        assertFalse(foundMerchandising.isActive());
    }

    private SearchHits<CustomChildIndex> findChildDocument(String parentId) {
        Query childQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .parentId(p -> p
                                .type("customChildIndex")
                                .id(parentId)
                        ))
                .withRoute(parentId)
                .build();

        return elasticsearchOperations.search(
                childQuery,
                CustomChildIndex.class,
                IndexCoordinates.of("statements")
        );
    }

    SearchHits<Statement> hasVotes() {
        Query query = NativeQuery.builder()
                .withQuery(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(qb -> qb
                        .hasChild(hc -> hc
                                .type("answer")
                                .queryName("vote")
                                .query(matchAllQueryAsQuery())
                                .scoreMode(ChildScoreMode.None)
                        )))
                .build();

        return elasticsearchOperations.search(query, Statement.class);
    }
}

@Setter
@Getter
@Builder
@Routing("routing")
@Document(indexName = "statements")
class Statement {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String text;

    @Field(type = FieldType.Keyword)
    private String routing;

    @JoinTypeRelations(
            relations =
                    {
                            @JoinTypeRelation(parent = "question", children = {"answer", "comment", "customChildIndex"}),
                            @JoinTypeRelation(parent = "answer", children = "vote")
                    }
    )
    private JoinField<String> relation;
}

@Setter
@Getter
@Builder
@Document(indexName = "statements")
class CustomChildIndex {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String customChildData;

    @Field(type = FieldType.Keyword)
    private String routing;

    @Field(type = FieldType.Keyword)
    private List<ApprovalInfo> approvalInfos;

    @Field(type = FieldType.Keyword)
    private List<ApprovalInfo> approvalInfoList;

    @JoinTypeRelations(relations = @JoinTypeRelation(parent = "question", children = {}))
    private JoinField<String> relation;
}

@Getter
@Setter
@Builder
class ApprovalInfo {

    @Field(type = FieldType.Keyword)
    private String departmentName;

    @Field(type = FieldType.Keyword)
    private String description;

    @Field(type = FieldType.Boolean)
    private boolean isActive;
}

interface StatementRepository extends ElasticsearchRepository<Statement, String> {

}
