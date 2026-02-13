package com.mb.livedataservice.integration_tests.config.serializer;

import com.mb.livedataservice.data.model.redis.dto.TestAddressDto;
import com.mb.livedataservice.data.model.redis.dto.TestCompanyDto;
import com.mb.livedataservice.data.model.redis.dto.TestDepartmentDto;
import com.mb.livedataservice.data.model.redis.dto.TestPersonDto;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import com.mb.livedataservice.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for CustomJackson2JsonRedisSerializer using actual Redis container.
 * Tests all cacheable structures: list, object, map, set, nested objects, and nested collections.
 */
@SpringBootTest(classes = TestcontainersConfiguration.class)
@DisplayName("Redis Integration Tests with CustomJackson2JsonRedisSerializer")
class RedisSerializerIntegrationTest {

    private static final String KEY_PREFIX = "test:serializer:";

    @Autowired
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        // Clean up test keys before each test
        Set<String> keys = cacheService.getKeys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            cacheService.deleteAll(keys);
        }
    }

    // ==================== Helper Methods ====================

    private TestPersonDto createTestPerson(Long id, String name) {
        return TestPersonDto.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase().replace(" ", ".") + "@test.com")
                .age(30)
                .active(true)
                .birthDate(LocalDate.of(1994, 5, 15))
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
                .build();
    }

    private TestAddressDto createTestAddress(Long id, String city) {
        return TestAddressDto.builder()
                .id(id)
                .street("123 Main Street")
                .city(city)
                .country("Turkey")
                .zipCode("34000")
                .build();
    }

    private TestCompanyDto createTestCompany() {
        List<TestPersonDto> employees = new ArrayList<>();
        employees.add(createTestPerson(1L, "John Doe"));
        employees.add(createTestPerson(2L, "Jane Smith"));

        Set<String> departments = new HashSet<>();
        departments.add("Engineering");
        departments.add("Marketing");
        departments.add("Sales");

        Map<String, TestPersonDto> managers = new HashMap<>();
        managers.put("eng", createTestPerson(3L, "Tech Lead"));
        managers.put("mkt", createTestPerson(4L, "Marketing Manager"));

        List<TestAddressDto> branches = new ArrayList<>();
        branches.add(createTestAddress(1L, "Istanbul"));
        branches.add(createTestAddress(2L, "Ankara"));

        return TestCompanyDto.builder()
                .id(1L)
                .name("Test Company")
                .industry("Technology")
                .headquarters(createTestAddress(100L, "Istanbul"))
                .employees(employees)
                .departments(departments)
                .managersById(managers)
                .branchAddresses(branches)
                .build();
    }

    private TestDepartmentDto createTestDepartment() {
        List<List<String>> teamGroups = new ArrayList<>();
        teamGroups.add(List.of("Team A1", "Team A2"));
        teamGroups.add(List.of("Team B1", "Team B2", "Team B3"));

        Map<String, List<TestPersonDto>> teamMembers = new HashMap<>();
        teamMembers.put("frontend", List.of(createTestPerson(1L, "Dev 1"), createTestPerson(2L, "Dev 2")));
        teamMembers.put("backend", List.of(createTestPerson(3L, "Dev 3")));

        List<Map<String, String>> configs = new ArrayList<>();
        Map<String, String> config1 = new LinkedHashMap<>();
        config1.put("key1", "value1");
        config1.put("key2", "value2");
        Map<String, String> config2 = new LinkedHashMap<>();
        config2.put("keyA", "valueA");
        configs.add(config1);
        configs.add(config2);

        return TestDepartmentDto.builder()
                .id(1L)
                .name("Engineering")
                .teamGroups(teamGroups)
                .teamMembers(teamMembers)
                .configurations(configs)
                .build();
    }

    // ==================== Single Object Tests ====================

    @Nested
    @DisplayName("Single Object Redis Operations")
    class SingleObjectRedisTests {

        @Test
        @DisplayName("Should store and retrieve a simple object from Redis")
        void shouldStoreAndRetrieveSimpleObject() {
            // Given
            String key = KEY_PREFIX + "person:1";
            TestPersonDto person = createTestPerson(1L, "John Doe");

            // When
            cacheService.put(key, person);
            TestPersonDto result = cacheService.get(key, TestPersonDto.class);

            // Then
            assertNotNull(result);
            assertEquals(person.getId(), result.getId());
            assertEquals(person.getName(), result.getName());
            assertEquals(person.getEmail(), result.getEmail());
            assertEquals(person.getAge(), result.getAge());
            assertEquals(person.getActive(), result.getActive());
            assertEquals(person.getBirthDate(), result.getBirthDate());
            assertEquals(person.getCreatedAt(), result.getCreatedAt());
        }

        @Test
        @DisplayName("Should store and retrieve an object with nested object from Redis")
        void shouldStoreAndRetrieveObjectWithNestedObject() {
            // Given
            String key = KEY_PREFIX + "company:nested";
            TestCompanyDto company = TestCompanyDto.builder()
                    .id(1L)
                    .name("Nested Test Company")
                    .industry("Tech")
                    .headquarters(createTestAddress(1L, "Istanbul"))
                    .build();

            // When
            cacheService.put(key, company);
            TestCompanyDto result = cacheService.get(key, TestCompanyDto.class);

            // Then
            assertNotNull(result);
            assertEquals(company.getId(), result.getId());
            assertEquals(company.getName(), result.getName());
            assertNotNull(result.getHeadquarters());
            assertEquals(company.getHeadquarters().getCity(), result.getHeadquarters().getCity());
        }

        @Test
        @DisplayName("Should store and retrieve object with TTL")
        void shouldStoreAndRetrieveObjectWithTTL() {
            // Given
            String key = KEY_PREFIX + "person:ttl";
            TestPersonDto person = createTestPerson(1L, "TTL User");

            // When
            cacheService.put(key, person, 1, TimeUnit.HOURS);
            TestPersonDto result = cacheService.get(key, TestPersonDto.class);

            // Then
            assertNotNull(result);
            assertTrue(cacheService.hasKey(key));
        }
    }

    // ==================== List Tests ====================

    @Nested
    @DisplayName("List Redis Operations")
    class ListRedisTests {

        @Test
        @DisplayName("Should store and retrieve a list of objects from Redis")
        void shouldStoreAndRetrieveListOfObjects() {
            // Given
            String key = KEY_PREFIX + "persons:list";
            List<TestPersonDto> persons = new ArrayList<>();
            persons.add(createTestPerson(1L, "John Doe"));
            persons.add(createTestPerson(2L, "Jane Smith"));
            persons.add(createTestPerson(3L, "Bob Wilson"));

            // When
            cacheService.put(key, persons);
            List<TestPersonDto> result = (List<TestPersonDto>) cacheService.get(key, List.class, TestPersonDto.class);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals("John Doe", result.get(0).getName());
            assertEquals("Jane Smith", result.get(1).getName());
            assertEquals("Bob Wilson", result.get(2).getName());
        }

        @Test
        @DisplayName("Should store and retrieve an empty list from Redis")
        void shouldStoreAndRetrieveEmptyList() {
            // Given
            String key = KEY_PREFIX + "empty:list";
            List<TestPersonDto> emptyList = new ArrayList<>();

            // When
            cacheService.put(key, emptyList);
            List<TestPersonDto> result = (List<TestPersonDto>) cacheService.get(key, List.class, TestPersonDto.class);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should store and retrieve list with nested objects from Redis")
        void shouldStoreAndRetrieveListWithNestedObjects() {
            // Given
            String key = KEY_PREFIX + "companies:list";
            List<TestCompanyDto> companies = new ArrayList<>();
            companies.add(createTestCompany());

            // When
            cacheService.put(key, companies);
            List<TestCompanyDto> result = (List<TestCompanyDto>) cacheService.get(key, List.class, TestCompanyDto.class);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertNotNull(result.getFirst().getHeadquarters());
            assertNotNull(result.getFirst().getEmployees());
        }
    }

    // ==================== Set Tests ====================

    @Nested
    @DisplayName("Set Redis Operations")
    class SetRedisTests {

        @Test
        @DisplayName("Should store and retrieve a set of objects from Redis")
        void shouldStoreAndRetrieveSetOfObjects() {
            // Given
            String key = KEY_PREFIX + "persons:set";
            Set<TestPersonDto> persons = new HashSet<>();
            persons.add(createTestPerson(1L, "John Doe"));
            persons.add(createTestPerson(2L, "Jane Smith"));

            // When
            cacheService.put(key, persons);
            Set<TestPersonDto> result = (Set<TestPersonDto>) cacheService.get(key, Set.class, TestPersonDto.class);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }

    // ==================== Map Tests ====================

    @Nested
    @DisplayName("Map Redis Operations")
    class MapRedisTests {

        @Test
        @DisplayName("Should store and retrieve a map with object values from Redis")
        void shouldStoreAndRetrieveMapWithObjectValues() {
            // Given
            String key = KEY_PREFIX + "persons:map";
            Map<String, TestPersonDto> personMap = new HashMap<>();
            personMap.put("person1", createTestPerson(1L, "John Doe"));
            personMap.put("person2", createTestPerson(2L, "Jane Smith"));

            // When
            cacheService.put(key, personMap);
            Map<String, TestPersonDto> result = (Map<String, TestPersonDto>) cacheService.get(key, Map.class);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey("person1"));
            assertTrue(result.containsKey("person2"));
            assertFalse(result.containsKey("@class"));
        }

        @Test
        @DisplayName("Should store and retrieve an empty map from Redis")
        void shouldStoreAndRetrieveEmptyMap() {
            // Given
            String key = KEY_PREFIX + "empty:map";
            Map<String, TestPersonDto> emptyMap = new HashMap<>();

            // When
            cacheService.put(key, emptyMap);
            Map<String, TestPersonDto> result = (Map<String, TestPersonDto>) cacheService.get(key, Map.class);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should store and retrieve a map with nested collections from Redis")
        void shouldStoreAndRetrieveMapWithNestedCollections() {
            // Given
            String key = KEY_PREFIX + "teams:map";
            Map<String, List<TestPersonDto>> teamMap = new HashMap<>();
            teamMap.put("team1", List.of(createTestPerson(1L, "Dev 1"), createTestPerson(2L, "Dev 2")));
            teamMap.put("team2", List.of(createTestPerson(3L, "Dev 3")));

            // When
            cacheService.put(key, teamMap);
            Map<String, List<TestPersonDto>> result = (Map<String, List<TestPersonDto>>) cacheService.get(key, Map.class);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey("team1"));
            assertTrue(result.containsKey("team2"));
            assertFalse(result.containsKey("@class"));

            assertInstanceOf(List.class, result.get("team1"));
            assertInstanceOf(List.class, result.get("team2"));
        }
    }

    // ==================== Nested Object Tests ====================

    @Nested
    @DisplayName("Nested Object Redis Operations")
    class NestedObjectRedisTests {

        @Test
        @DisplayName("Should store and retrieve object with multiple levels of nesting from Redis")
        void shouldStoreAndRetrieveObjectWithMultipleLevelsOfNesting() {
            // Given
            String key = KEY_PREFIX + "company:complex";
            TestCompanyDto company = createTestCompany();

            // When
            cacheService.put(key, company);
            TestCompanyDto result = cacheService.get(key, TestCompanyDto.class);

            // Then
            assertNotNull(result);
            assertEquals(company.getId(), result.getId());
            assertEquals(company.getName(), result.getName());

            // Verify nested object
            assertNotNull(result.getHeadquarters());
            assertEquals(company.getHeadquarters().getCity(), result.getHeadquarters().getCity());

            // Verify nested list
            assertNotNull(result.getEmployees());
            assertEquals(2, result.getEmployees().size());

            // Verify nested set (deserialized as collection)
            assertNotNull(result.getDepartments());

            // Verify nested map
            assertNotNull(result.getManagersById());

            // Verify nested list of objects
            assertNotNull(result.getBranchAddresses());
            assertEquals(2, result.getBranchAddresses().size());
        }

        @Test
        @DisplayName("Should store and retrieve object with null nested fields from Redis")
        void shouldStoreAndRetrieveObjectWithNullNestedFields() {
            // Given
            String key = KEY_PREFIX + "company:nulls";
            TestCompanyDto company = TestCompanyDto.builder()
                    .id(1L)
                    .name("Simple Company")
                    .industry("Tech")
                    .headquarters(null)
                    .employees(null)
                    .departments(null)
                    .managersById(null)
                    .branchAddresses(null)
                    .build();

            // When
            cacheService.put(key, company);
            TestCompanyDto result = cacheService.get(key, TestCompanyDto.class);

            // Then
            assertNotNull(result);
            assertEquals(company.getId(), result.getId());
            assertNull(result.getHeadquarters());
            assertNull(result.getEmployees());
        }
    }

    // ==================== Nested Collections Tests ====================

    @Nested
    @DisplayName("Nested Collections Redis Operations")
    class NestedCollectionsRedisTests {

        @Test
        @DisplayName("Should store and retrieve deeply nested collections from Redis")
        void shouldStoreAndRetrieveDeeplyNestedCollections() {
            // Given
            String key = KEY_PREFIX + "department:nested";
            TestDepartmentDto department = createTestDepartment();

            // When
            cacheService.put(key, department);
            TestDepartmentDto result = cacheService.get(key, TestDepartmentDto.class);

            // Then
            assertNotNull(result);
            assertEquals(department.getId(), result.getId());
            assertEquals(department.getName(), result.getName());

            // Verify List of Lists
            assertNotNull(result.getTeamGroups());
            assertEquals(2, result.getTeamGroups().size());

            // Verify Map with List values
            assertNotNull(result.getTeamMembers());

            // Verify List of Maps
            assertNotNull(result.getConfigurations());
            assertEquals(2, result.getConfigurations().size());
        }

        @Test
        @DisplayName("Should store and retrieve list of departments with nested collections from Redis")
        void shouldStoreAndRetrieveListOfDepartmentsWithNestedCollections() {
            // Given
            String key = KEY_PREFIX + "departments:list";
            List<TestDepartmentDto> departments = new ArrayList<>();
            departments.add(createTestDepartment());
            departments.add(TestDepartmentDto.builder()
                    .id(2L)
                    .name("Marketing")
                    .teamGroups(List.of(List.of("Campaign Team")))
                    .build());

            // When
            cacheService.put(key, departments);
            List<TestDepartmentDto> result = (List<TestDepartmentDto>) cacheService.get(key, List.class, TestDepartmentDto.class);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Engineering", result.get(0).getName());
            assertEquals("Marketing", result.get(1).getName());
        }
    }

    // ==================== Round-Trip Tests ====================

    @Nested
    @DisplayName("Round-Trip Redis Operations")
    class RoundTripRedisTests {

        @Test
        @DisplayName("Should maintain data integrity through multiple store/retrieve cycles")
        void shouldMaintainDataIntegrityThroughMultipleCycles() {
            // Given
            String key = KEY_PREFIX + "roundtrip:company";
            TestCompanyDto original = createTestCompany();

            // When - First cycle
            cacheService.put(key, original);
            TestCompanyDto retrieved1 = cacheService.get(key, TestCompanyDto.class);
            assertNotNull(retrieved1);

            // Second cycle - store retrieved data again
            cacheService.put(key, retrieved1);
            TestCompanyDto retrieved2 = cacheService.get(key, TestCompanyDto.class);
            assertNotNull(retrieved2);

            // Third cycle
            cacheService.put(key, retrieved2);
            TestCompanyDto retrieved3 = cacheService.get(key, TestCompanyDto.class);
            assertNotNull(retrieved3);

            // Then
            assertEquals(original.getId(), retrieved3.getId());
            assertEquals(original.getName(), retrieved3.getName());
            assertEquals(original.getHeadquarters().getCity(), retrieved3.getHeadquarters().getCity());
            assertEquals(original.getEmployees().size(), retrieved3.getEmployees().size());
        }

        @Test
        @DisplayName("Should handle concurrent reads and writes")
        void shouldHandleConcurrentReadsAndWrites() throws InterruptedException {
            // Given
            String key = KEY_PREFIX + "concurrent:person";
            TestPersonDto person = createTestPerson(1L, "Concurrent User");

            // When - Write
            cacheService.put(key, person);

            // Multiple concurrent reads
            Thread[] threads = new Thread[5];
            TestPersonDto[] results = new TestPersonDto[5];

            for (int i = 0; i < 5; i++) {
                final int index = i;
                threads[i] = new Thread(() -> results[index] = cacheService.get(key, TestPersonDto.class));
                threads[i].start();
            }

            // Wait for all threads
            for (Thread thread : threads) {
                thread.join();
            }

            // Then - All reads should return the same data
            for (TestPersonDto result : results) {
                assertNotNull(result);
                assertEquals("Concurrent User", result.getName());
            }
        }
    }

    // ==================== Delete and Exists Tests ====================

    @Nested
    @DisplayName("Delete and Exists Redis Operations")
    class DeleteAndExistsRedisTests {

        @Test
        @DisplayName("Should delete cached object")
        void shouldDeleteCachedObject() {
            // Given
            String key = KEY_PREFIX + "delete:person";
            TestPersonDto person = createTestPerson(1L, "Delete User");
            cacheService.put(key, person);

            // When
            Boolean deleted = cacheService.delete(key);

            // Then
            assertTrue(deleted);
            assertFalse(cacheService.hasKey(key));
        }

        @Test
        @DisplayName("Should check if key exists")
        void shouldCheckIfKeyExists() {
            // Given
            String key = KEY_PREFIX + "exists:person";
            TestPersonDto person = createTestPerson(1L, "Exists User");

            // When - Before setting
            boolean existsBefore = cacheService.hasKey(key);

            cacheService.put(key, person);

            // When - After setting
            boolean existsAfter = cacheService.hasKey(key);

            // Then
            assertFalse(existsBefore);
            assertTrue(existsAfter);
        }
    }
}
