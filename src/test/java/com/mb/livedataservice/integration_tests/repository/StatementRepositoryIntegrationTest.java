package com.mb.livedataservice.integration_tests.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.JoinTypeRelation;
import org.springframework.data.elasticsearch.annotations.JoinTypeRelations;
import org.springframework.data.elasticsearch.annotations.Routing;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.cluster.ClusterHealth;
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
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.elasticsearch.client.elc.Queries.matchAllQueryAsQuery;

interface StatementRepository extends ElasticsearchRepository<Statement, String> {

}

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestcontainersConfiguration.class)
class StatementRepositoryIntegrationTest {

    private static final String STATEMENTS_INDEX = "statements";
    private static final String PARENT_QUESTION = "question";
    private static final String CUSTOM_CHILD_INDEX = "customChildIndex";
    private static Statement savedWeather;
    private static List<ApprovalInfo> approvalInfos;
    private static List<ApprovalInfo> approvalInfoList;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeAll
    void setUp() {
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

        approvalInfos = List.of(quality, supply);
        approvalInfoList = List.of(supply, merchandising);

        savedWeather = statementRepository.save(
                Statement.builder()
                        .id("1")
                        .text("How is the weather?")
                        .relation(new JoinField<>(PARENT_QUESTION))
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

        statementRepository.save(
                Statement.builder()
                        .id("8")
                        .text("I don't like the rain")
                        .routing("searchRouting")
                        .build());
    }

    @AfterAll
    void tearDown() {
        long countBefore = statementRepository.count();
        assertEquals(20, countBefore, "There should be 20 documents after the init");

        statementRepository.deleteAll();

        long countAfter = statementRepository.count();
        assertEquals(0, countAfter, "The statementRepository should be empty after deleteAll");
    }

    @Test
    void saveAndFind_ShouldFindStatement_WhenStatementExists() {
        Statement statement = Statement.builder()
                .id("1")
                .text("How is the weather?")
                .relation(new JoinField<>(PARENT_QUESTION))
                .routing("testRouting")
                .build();

        Statement savedStatement = statementRepository.save(statement);

        Optional<Statement> foundStatement = statementRepository.findById(savedStatement.getId());
        assertTrue(foundStatement.isPresent());
        assertEquals("How is the weather?", foundStatement.get().getText());
        assertEquals("testRouting", foundStatement.get().getRouting());
    }

    @Test
    void saveMultiple_ShouldSaveAllStatements_WhenGivenMultipleStatements() {
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
    void search_ShouldFindMatchingStatements_WhenSearchingByText() {
        assertEquals(1, elasticsearchOperations.count(new CriteriaQuery(Criteria.where("text").is("I don't like the rain")), Statement.class));
    }

    @Test
    void saveStatements_ShouldPersistAllFields_WhenSavingMultipleTypes() {
        Statement weather = statementRepository.findById("1").orElse(null);
        assertNotNull(weather, "The 'How is the weather?' question should be saved");
        assertEquals("How is the weather?", weather.getText());
        assertEquals(PARENT_QUESTION, weather.getRelation().getName());

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
    void verifyRelations_ShouldHaveCorrectRelationships_WhenDocumentsAreSaved() {
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
    void findByRouting_ShouldReturnStatement_WhenRoutingExists() {
        Statement vote = statementRepository.findById("5").orElse(null);
        assertNotNull(vote, "The vote statement should be found using routing");
        assertEquals("+1 for the sun", vote.getText());
    }

    @Test
    void searchByText_ShouldReturnMatches_WhenTextExists() {
        long count = elasticsearchOperations.count(CriteriaQuery.builder(Criteria.where("text").is("sunny")).build(), Statement.class);
        assertEquals(1, count, "There should be 1 statement with the text 'sunny'");

        count = elasticsearchOperations.count(CriteriaQuery.builder(Criteria.where("text").is("How is the weather?")).build(), Statement.class);
        assertEquals(1, count, "There should be 1 statement with the text 'How is the weather?'");
    }

    @Test
    void searchByRelation_ShouldReturnMatches_WhenRelationExists() {
        long count = elasticsearchOperations.count(CriteriaQuery.builder(Criteria.where("relation").is(PARENT_QUESTION)).build(), Statement.class);
        assertEquals(7, count, "There should be 7 statement with relation 'question'");

        count = elasticsearchOperations.count(CriteriaQuery.builder(Criteria.where("relation").is("answer")).build(), Statement.class);
        assertEquals(2, count, "There should be 2 statements with relation 'answer'");
    }

    @Test
    void delete_ShouldRemoveStatement_WhenStatementExists() {
        Statement statementToDelete = statementRepository.findById("8").orElse(null);
        assertNotNull(statementToDelete, "The statement to delete should exist");

        statementRepository.delete(statementToDelete);
        Optional<Statement> deletedStatement = statementRepository.findById("8");
        assertTrue(deletedStatement.isEmpty(), "The statement should be deleted");
    }

    @Test
    void hasVotes_ShouldReturnTrue_WhenVotesExist() {
        long count = hasVotes().stream().count();
        assertEquals(1, count, "There should be 1 vote");
    }

    @Test
    void saveCustomChildIndexWithApprovalInfo_ShouldCreateRelationship_WhenSavingChild() {
        // Save parent statement
        Statement parentStatement = Statement.builder()
                .id("customChildIndex-parent")
                .text("Parent Statement")
                .relation(new JoinField<>(PARENT_QUESTION))
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
                .relation(new JoinField<>(CUSTOM_CHILD_INDEX, parentStatement.getId()))
                .approvalInfos(List.of(quality, supply))
                .approvalInfoList(List.of(supply, merchandising))
                .build();

        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(customChildIndex.getId())
                .withRouting(parentStatement.getId())
                .withObject(customChildIndex)
                .build();

        elasticsearchOperations.index(indexQuery, IndexCoordinates.of(STATEMENTS_INDEX));
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

    @Test
    void checkClusterHealth_ShouldBeHealthy_WhenElasticsearchIsRunning() {
        // Arrange
        // Act
        ClusterHealth health = elasticsearchOperations.cluster().health();

        // Assertions
        assertTrue(health.getStatus().equals("green") || health.getStatus().equalsIgnoreCase("yellow"), "Cluster health should be green or yellow, but was: " + health.getStatus());
        assertNotNull(health.getClusterName(), "Cluster name should not be null");
        assertTrue(health.getNumberOfNodes() > 0, "Cluster should have at least one node");
        assertTrue(health.getActiveShards() > 0, "Cluster should have active shards");
    }

    @Test
    void saveAndFind_ShouldReturnStatement_WhenStatementExists() {
        // Arrange
        Statement statement = Statement.builder()
                .id("15")
                .text("Another Statement")
                .routing("15")
                .relation(new JoinField<>(PARENT_QUESTION))
                .build();

        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(statement.getId())
                .withRouting(statement.getRouting())
                .withObject(statement)
                .build();

        elasticsearchOperations.index(indexQuery, IndexCoordinates.of(STATEMENTS_INDEX));
        elasticsearchOperations.indexOps(Statement.class).refresh();

        // Act
        Query query = NativeQuery.builder()
                .withIds(statement.getId())
                .build();
        SearchHits<Statement> searchHits = elasticsearchOperations.search(query, Statement.class);

        // Assertions
        assertNotNull(searchHits.getSearchHits());
        assertFalse(searchHits.getSearchHits().isEmpty());
        assertTrue(searchHits.get().anyMatch(statementSearchHit -> "15".equals(statementSearchHit.getContent().getId())));
        assertTrue(searchHits.get().anyMatch(statementSearchHit -> "Another Statement".equals(statementSearchHit.getContent().getText())));
    }

    @Test
    void findAll_ShouldReturnAllStatements_WhenMultipleStatementsExist() {
        // Arrange
        Statement firstParent = prepareStatement("16", "Statement 1");
        Statement secondParent = prepareStatement("17", "Statement 2");

        IndexQuery firstParentIndexQuery = new IndexQueryBuilder()
                .withId(firstParent.getId())
                .withRouting(firstParent.getId())
                .withObject(firstParent)
                .build();

        elasticsearchOperations.index(firstParentIndexQuery, IndexCoordinates.of(STATEMENTS_INDEX));

        IndexQuery secondParentIndexQuery = new IndexQueryBuilder()
                .withId(secondParent.getId())
                .withRouting(secondParent.getId())
                .withObject(secondParent)
                .build();

        elasticsearchOperations.index(secondParentIndexQuery, IndexCoordinates.of(STATEMENTS_INDEX));

        CustomChildIndex firstChild = prepareCustomChildIndex("18", firstParent, approvalInfos, approvalInfoList);
        CustomChildIndex secondChild = prepareCustomChildIndex("19", secondParent, approvalInfos, approvalInfoList);

        IndexQuery firstChildIndexQuery = new IndexQueryBuilder()
                .withId(firstChild.getId())
                .withRouting(firstParent.getRouting())
                .withObject(firstChild)
                .build();

        elasticsearchOperations.index(firstChildIndexQuery, IndexCoordinates.of(STATEMENTS_INDEX));

        IndexQuery secondChildIndexQuery = new IndexQueryBuilder()
                .withId(secondChild.getId())
                .withRouting(secondParent.getRouting())
                .withObject(secondChild)
                .build();

        elasticsearchOperations.index(secondChildIndexQuery, IndexCoordinates.of(STATEMENTS_INDEX));
        elasticsearchOperations.indexOps(Statement.class).refresh();

        // Act
        Query query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .withPageable(PageRequest.of(0, 20)) // Use Pageable to control size
                .build();
        SearchHits<Statement> searchHits = elasticsearchOperations.search(query, Statement.class);

        // Assertions
        assertTrue(searchHits.getTotalHits() >= 2);
        List<Statement> statements = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();

        // Verify first statement
        assertTrue(statements.stream().anyMatch(p -> p.getId().equals("16") && "Statement 1".equals(p.getText())));

        // Verify second statement
        SearchHits<Statement> secondParentResults = findParentDocument(secondParent.getId());

        assertEquals(1, secondParentResults.getTotalHits());
        assertTrue(secondParentResults.getSearchHits().stream().anyMatch(hit -> hit.getContent().getId().equals("17")));
        assertEquals("Statement 2", secondParentResults.getSearchHit(0).getContent().getText());

        // Verify join fields
        assertTrue(statements.stream()
                .map(Statement::getRelation)
                .filter(Objects::nonNull)
                .anyMatch(relation -> PARENT_QUESTION.equals(relation.getName()) || CUSTOM_CHILD_INDEX.equals(relation.getName())));
    }

    @Test
    void saveChildDocument_ShouldCreateParentChildRelationship_WhenSavingApprovalStatus() {
        // Arrange
        Statement parent = prepareStatement("20", "Statement 3");

        IndexQuery parentIndexQuery = new IndexQueryBuilder()
                .withId(parent.getId())
                .withRouting(parent.getId())
                .withObject(parent)
                .build();

        elasticsearchOperations.index(parentIndexQuery, IndexCoordinates.of(STATEMENTS_INDEX));

        CustomChildIndex firstChild = prepareCustomChildIndex("21", parent, approvalInfos, approvalInfoList);
        CustomChildIndex secondChild = prepareCustomChildIndex("22", parent, approvalInfos, approvalInfoList);

        List.of(firstChild, secondChild)
                .forEach(child -> {
                    IndexQuery indexQuery = new IndexQueryBuilder()
                            .withId(child.getId())
                            .withRouting(parent.getId())
                            .withObject(child)
                            .build();

                    elasticsearchOperations.index(indexQuery, IndexCoordinates.of(STATEMENTS_INDEX));
                });
        elasticsearchOperations.indexOps(Statement.class).refresh();

        // Act
        SearchHits<CustomChildIndex> childResults = findChildDocument(parent.getId());

        // Assertions
        assertEquals(2, childResults.getTotalHits());
        assertTrue(childResults.getSearchHits().stream().anyMatch(hit -> hit.getContent().getId().equals("21")));
        assertTrue(childResults.getSearchHits().stream().anyMatch(hit -> hit.getContent().getId().equals("22")));

        CustomChildIndex foundFirstChild = childResults.getSearchHit(0).getContent();
        CustomChildIndex foundSecondChild = childResults.getSearchHit(1).getContent();

        assertEquals(2, foundFirstChild.getApprovalInfos().size());
        assertEquals(2, foundFirstChild.getApprovalInfoList().size());

        assertEquals(2, foundSecondChild.getApprovalInfos().size());
        assertEquals(2, foundSecondChild.getApprovalInfoList().size());
    }

    @Test
    void findCustomChildIndexByParentWithFuzzyMatch_ShouldReturnMatchingIndexes_WhenSearchingWithFuzzy() {
        // Arrange
        Statement parent = prepareStatement("23", "Parent Statement");
        IndexQuery parentIndexQuery = new IndexQueryBuilder()
                .withId(parent.getId())
                .withRouting(parent.getId())
                .withObject(parent)
                .build();
        elasticsearchOperations.index(parentIndexQuery, IndexCoordinates.of(STATEMENTS_INDEX));

        // Create children with approval departments
        CustomChildIndex firstChild = prepareCustomChildIndex("24", parent,
                List.of(
                        ApprovalInfo.builder()
                                .departmentName("Quality")
                                .description("Quality Department")
                                .isActive(true)
                                .build()
                ),
                List.of(
                        ApprovalInfo.builder()
                                .departmentName("Merchandising")
                                .description("Merchandising Department")
                                .isActive(false)
                                .build()
                ));

        CustomChildIndex secondChild = prepareCustomChildIndex("25", parent,
                List.of(
                        ApprovalInfo.builder()
                                .departmentName("Supply")
                                .description("Supply Department")
                                .isActive(true)
                                .build()
                ),
                List.of(
                        ApprovalInfo.builder()
                                .departmentName("IT")
                                .description("IT Department")
                                .isActive(false)
                                .build()
                ));

        List.of(firstChild, secondChild)
                .forEach(child -> {
                            IndexQuery childIndexQuery = new IndexQueryBuilder()
                                    .withId(child.getId())
                                    .withRouting(parent.getId())
                                    .withObject(child)
                                    .build();
                            elasticsearchOperations.index(childIndexQuery, IndexCoordinates.of(STATEMENTS_INDEX));
                        }
                );

        elasticsearchOperations.indexOps(Statement.class).refresh();

        // Act
        SearchHits<CustomChildIndex> results = findCustomChildIndexByParentWithFuzzyMatch(parent.getId());

        // Assertions
        assertNotNull(results);
        assertEquals(2, results.getTotalHits());

        List<CustomChildIndex> foundChildren = results.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // Verify first child
        assertTrue(foundChildren.stream()
                .anyMatch(child -> child.getId().equals("24")
                        && child.getApprovalInfos().stream().anyMatch(info -> info.getDepartmentName().equals("Quality"))
                        && child.getApprovalInfoList().stream().anyMatch(info -> info.getDepartmentName().equals("Merchandising"))));

        // Verify second child
        assertTrue(foundChildren.stream()
                .anyMatch(child -> child.getId().equals("25")
                        && child.getApprovalInfos().stream().anyMatch(info -> info.getDepartmentName().equals("Supply"))
                        && child.getApprovalInfoList().stream().anyMatch(info -> info.getDepartmentName().equals("IT"))));
    }

    private SearchHits<CustomChildIndex> findCustomChildIndexByParentWithFuzzyMatch(String parentId) {
        Query childQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .parentId(p -> p
                                                .type(CUSTOM_CHILD_INDEX)
                                                .id(parentId)
                                        )
                                )
                                .should(s -> s
                                        .fuzzy(f -> f
                                                .field("approvalInfos.departmentName")
                                                .value("*")
                                                .fuzziness("AUTO")
                                        )
                                )
                        )
                )
                .withRoute(parentId)
                .build();

        return elasticsearchOperations.search(childQuery, CustomChildIndex.class, IndexCoordinates.of(STATEMENTS_INDEX));
    }

    private Statement prepareStatement(String id, String text) {
        return Statement.builder()
                .id(id)
                .text(text)
                .routing(id)
                .relation(new JoinField<>(PARENT_QUESTION))
                .build();
    }

    private CustomChildIndex prepareCustomChildIndex(String id, Statement parent, List<ApprovalInfo> approvalInfos, List<ApprovalInfo> approvalInfoList) {
        return CustomChildIndex.builder()
                .id(id)
                .routing(parent.getId())
                .approvalInfos(approvalInfos)
                .approvalInfoList(approvalInfoList)
                .relation(new JoinField<>(CUSTOM_CHILD_INDEX, parent.getId()))
                .build();
    }

    private SearchHits<CustomChildIndex> findChildDocument(String parentId) {
        Query childQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .parentId(p -> p
                                .type(CUSTOM_CHILD_INDEX)
                                .id(parentId)
                        ))
                .withRoute(parentId)
                .build();

        return elasticsearchOperations.search(childQuery, CustomChildIndex.class, IndexCoordinates.of(STATEMENTS_INDEX));
    }

    private SearchHits<Statement> findParentDocument(String parentId) {
        Query parentQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t
                                .field("_id")
                                .value(parentId)
                        ))
                .withRoute(parentId)
                .build();

        return elasticsearchOperations.search(parentQuery, Statement.class, IndexCoordinates.of(STATEMENTS_INDEX));
    }

    private SearchHits<Statement> hasVotes() {
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
@NoArgsConstructor
@AllArgsConstructor
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
@NoArgsConstructor
@AllArgsConstructor
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
