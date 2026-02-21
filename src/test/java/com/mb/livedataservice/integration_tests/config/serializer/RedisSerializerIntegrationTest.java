package com.mb.livedataservice.integration_tests.config.serializer;

import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import com.mb.livedataservice.service.CacheService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
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
 * Uses inner static classes instead of external Test DTOs to keep the test self-contained.
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

    private Person createTestPerson(Long id, String name) {
        return Person.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase().replace(" ", ".") + "@test.com")
                .age(30)
                .active(true)
                .birthDate(LocalDate.of(1994, 5, 15))
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
                .build();
    }

    private Address createTestAddress(Long id, String city) {
        return Address.builder()
                .id(id)
                .street("123 Main Street")
                .city(city)
                .country("Turkey")
                .zipCode("34000")
                .build();
    }

    private Company createTestCompany() {
        List<Person> employees = new ArrayList<>();
        employees.add(createTestPerson(1L, "John Doe"));
        employees.add(createTestPerson(2L, "Jane Smith"));

        Set<String> departments = new HashSet<>();
        departments.add("Engineering");
        departments.add("Marketing");
        departments.add("Sales");

        Map<String, Person> managers = new HashMap<>();
        managers.put("eng", createTestPerson(3L, "Tech Lead"));
        managers.put("mkt", createTestPerson(4L, "Marketing Manager"));

        List<Address> branches = new ArrayList<>();
        branches.add(createTestAddress(1L, "Istanbul"));
        branches.add(createTestAddress(2L, "Ankara"));

        return Company.builder()
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

    private Department createTestDepartment() {
        List<List<String>> teamGroups = new ArrayList<>();
        teamGroups.add(List.of("Team A1", "Team A2"));
        teamGroups.add(List.of("Team B1", "Team B2", "Team B3"));

        Map<String, List<Person>> teamMembers = new HashMap<>();
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

        return Department.builder()
                .id(1L)
                .name("Engineering")
                .teamGroups(teamGroups)
                .teamMembers(teamMembers)
                .configurations(configs)
                .build();
    }

    // ==================== Inner Test Model Classes ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Person implements Serializable {
        private Long id;
        private String name;
        private String email;
        private Integer age;
        private Boolean active;
        private LocalDate birthDate;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Address implements Serializable {
        private Long id;
        private String street;
        private String city;
        private String country;
        private String zipCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Company implements Serializable {
        private Long id;
        private String name;
        private String industry;
        private Address headquarters;
        private List<Person> employees;
        private Set<String> departments;
        private Map<String, Person> managersById;
        private List<Address> branchAddresses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Department implements Serializable {
        private Long id;
        private String name;
        private List<List<String>> teamGroups;
        private Map<String, List<Person>> teamMembers;
        private List<Map<String, String>> configurations;
    }

    // ==================== Single Object Tests ====================

    @Nested
    @DisplayName("Single Object Redis Operations")
    class SingleObjectRedisTests {

        @Test
        @DisplayName("Should store and retrieve a simple object from Redis")
        void put_ShouldStoreAndRetrieve_WhenSimpleObject() {
            // Arrange
            String key = KEY_PREFIX + "person:1";
            Person person = createTestPerson(1L, "John Doe");

            // Act
            cacheService.put(key, person);
            Person result = cacheService.get(key, Person.class);

            // Assertions
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
        void put_ShouldStoreAndRetrieve_WhenObjectWithNestedObject() {
            // Arrange
            String key = KEY_PREFIX + "company:nested";
            Company company = Company.builder()
                    .id(1L)
                    .name("Nested Test Company")
                    .industry("Tech")
                    .headquarters(createTestAddress(1L, "Istanbul"))
                    .build();

            // Act
            cacheService.put(key, company);
            Company result = cacheService.get(key, Company.class);

            // Assertions
            assertNotNull(result);
            assertEquals(company.getId(), result.getId());
            assertEquals(company.getName(), result.getName());
            assertNotNull(result.getHeadquarters());
            assertEquals(company.getHeadquarters().getCity(), result.getHeadquarters().getCity());
        }

        @Test
        @DisplayName("Should store and retrieve object with TTL")
        void put_ShouldStoreWithTTL_WhenObjectWithTimeUnit() {
            // Arrange
            String key = KEY_PREFIX + "person:ttl";
            Person person = createTestPerson(1L, "TTL User");

            // Act
            cacheService.put(key, person, 1, TimeUnit.HOURS);
            Person result = cacheService.get(key, Person.class);

            // Assertions
            assertNotNull(result);
            assertTrue(cacheService.hasKey(key));
        }
    }

    @Nested
    @DisplayName("List Redis Operations")
    class ListRedisTests {

        @Test
        @DisplayName("Should store and retrieve a list of objects from Redis")
        void put_ShouldStoreAndRetrieve_WhenListOfObjects() {
            // Arrange
            String key = KEY_PREFIX + "persons:list";
            List<Person> persons = new ArrayList<>();
            persons.add(createTestPerson(1L, "John Doe"));
            persons.add(createTestPerson(2L, "Jane Smith"));
            persons.add(createTestPerson(3L, "Bob Wilson"));

            // Act
            cacheService.put(key, persons);
            List<Person> result = (List<Person>) cacheService.get(key, List.class, Person.class);

            // Assertions
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals("John Doe", result.get(0).getName());
            assertEquals("Jane Smith", result.get(1).getName());
            assertEquals("Bob Wilson", result.get(2).getName());
        }

        @Test
        @DisplayName("Should store and retrieve an empty list from Redis")
        void put_ShouldStoreAndRetrieve_WhenEmptyList() {
            // Arrange
            String key = KEY_PREFIX + "empty:list";
            List<Person> emptyList = new ArrayList<>();

            // Act
            cacheService.put(key, emptyList);
            List<Person> result = (List<Person>) cacheService.get(key, List.class, Person.class);

            // Assertions
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should store and retrieve list with nested objects from Redis")
        void put_ShouldStoreAndRetrieve_WhenListWithNestedObjects() {
            // Arrange
            String key = KEY_PREFIX + "companies:list";
            List<Company> companies = new ArrayList<>();
            companies.add(createTestCompany());

            // Act
            cacheService.put(key, companies);
            List<Company> result = (List<Company>) cacheService.get(key, List.class, Company.class);

            // Assertions
            assertNotNull(result);
            assertEquals(1, result.size());
            assertNotNull(result.getFirst().getHeadquarters());
            assertNotNull(result.getFirst().getEmployees());
        }
    }

    @Nested
    @DisplayName("Set Redis Operations")
    class SetRedisTests {

        @Test
        @DisplayName("Should store and retrieve a set of objects from Redis")
        void put_ShouldStoreAndRetrieve_WhenSetOfObjects() {
            // Arrange
            String key = KEY_PREFIX + "persons:set";
            Set<Person> persons = new HashSet<>();
            persons.add(createTestPerson(1L, "John Doe"));
            persons.add(createTestPerson(2L, "Jane Smith"));

            // Act
            cacheService.put(key, persons);
            Set<Person> result = (Set<Person>) cacheService.get(key, Set.class, Person.class);

            // Assertions
            assertNotNull(result);
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("Map Redis Operations")
    class MapRedisTests {

        @Test
        @DisplayName("Should store and retrieve a map with object values from Redis")
        void put_ShouldStoreAndRetrieve_WhenMapWithObjectValues() {
            // Arrange
            String key = KEY_PREFIX + "persons:map";
            Map<String, Person> personMap = new HashMap<>();
            personMap.put("person1", createTestPerson(1L, "John Doe"));
            personMap.put("person2", createTestPerson(2L, "Jane Smith"));

            // Act
            cacheService.put(key, personMap);
            Map<String, Person> result = (Map<String, Person>) cacheService.get(key, Map.class);

            // Assertions
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey("person1"));
            assertTrue(result.containsKey("person2"));
            assertFalse(result.containsKey("@class"));
        }

        @Test
        @DisplayName("Should store and retrieve an empty map from Redis")
        void put_ShouldStoreAndRetrieve_WhenEmptyMap() {
            // Arrange
            String key = KEY_PREFIX + "empty:map";
            Map<String, Person> emptyMap = new HashMap<>();

            // Act
            cacheService.put(key, emptyMap);
            Map<String, Person> result = (Map<String, Person>) cacheService.get(key, Map.class);

            // Assertions
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should store and retrieve a map with nested collections from Redis")
        void put_ShouldStoreAndRetrieve_WhenMapWithNestedCollections() {
            // Arrange
            String key = KEY_PREFIX + "teams:map";
            Map<String, List<Person>> teamMap = new HashMap<>();
            teamMap.put("team1", List.of(createTestPerson(1L, "Dev 1"), createTestPerson(2L, "Dev 2")));
            teamMap.put("team2", List.of(createTestPerson(3L, "Dev 3")));

            // Act
            cacheService.put(key, teamMap);
            Map<String, List<Person>> result = (Map<String, List<Person>>) cacheService.get(key, Map.class);

            // Assertions
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey("team1"));
            assertTrue(result.containsKey("team2"));
            assertFalse(result.containsKey("@class"));

            assertInstanceOf(List.class, result.get("team1"));
            assertInstanceOf(List.class, result.get("team2"));
        }
    }

    // ==================== Single Object Tests ====================

    @Nested
    @DisplayName("Nested Object Redis Operations")
    class NestedObjectRedisTests {

        @Test
        @DisplayName("Should store and retrieve object with multiple levels of nesting from Redis")
        void put_ShouldStoreAndRetrieve_WhenObjectWithMultipleLevelsOfNesting() {
            // Arrange
            String key = KEY_PREFIX + "company:complex";
            Company company = createTestCompany();

            // Act
            cacheService.put(key, company);
            Company result = cacheService.get(key, Company.class);

            // Assertions
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
        void put_ShouldStoreAndRetrieve_WhenObjectWithNullNestedFields() {
            // Arrange
            String key = KEY_PREFIX + "company:nulls";
            Company company = Company.builder()
                    .id(1L)
                    .name("Simple Company")
                    .industry("Tech")
                    .headquarters(null)
                    .employees(null)
                    .departments(null)
                    .managersById(null)
                    .branchAddresses(null)
                    .build();

            // Act
            cacheService.put(key, company);
            Company result = cacheService.get(key, Company.class);

            // Assertions
            assertNotNull(result);
            assertEquals(company.getId(), result.getId());
            assertNull(result.getHeadquarters());
            assertNull(result.getEmployees());
        }
    }

    // ==================== List Tests ====================

    @Nested
    @DisplayName("Nested Collections Redis Operations")
    class NestedCollectionsRedisTests {

        @Test
        @DisplayName("Should store and retrieve deeply nested collections from Redis")
        void put_ShouldStoreAndRetrieve_WhenDeeplyNestedCollections() {
            // Arrange
            String key = KEY_PREFIX + "department:nested";
            Department department = createTestDepartment();

            // Act
            cacheService.put(key, department);
            Department result = cacheService.get(key, Department.class);

            // Assertions
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
        void put_ShouldStoreAndRetrieve_WhenListOfDepartmentsWithNestedCollections() {
            // Arrange
            String key = KEY_PREFIX + "departments:list";
            List<Department> departments = new ArrayList<>();
            departments.add(createTestDepartment());
            departments.add(Department.builder()
                    .id(2L)
                    .name("Marketing")
                    .teamGroups(List.of(List.of("Campaign Team")))
                    .build());

            // Act
            cacheService.put(key, departments);
            List<Department> result = (List<Department>) cacheService.get(key, List.class, Department.class);

            // Assertions
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
        void put_ShouldMaintainDataIntegrity_WhenMultipleCycles() {
            // Arrange
            String key = KEY_PREFIX + "roundtrip:company";
            Company original = createTestCompany();

            // Act - First cycle
            cacheService.put(key, original);
            Company retrieved1 = cacheService.get(key, Company.class);
            assertNotNull(retrieved1);

            // Second cycle - store retrieved data again
            cacheService.put(key, retrieved1);
            Company retrieved2 = cacheService.get(key, Company.class);
            assertNotNull(retrieved2);

            // Third cycle
            cacheService.put(key, retrieved2);
            Company retrieved3 = cacheService.get(key, Company.class);
            assertNotNull(retrieved3);

            // Assertions
            assertEquals(original.getId(), retrieved3.getId());
            assertEquals(original.getName(), retrieved3.getName());
            assertEquals(original.getHeadquarters().getCity(), retrieved3.getHeadquarters().getCity());
            assertEquals(original.getEmployees().size(), retrieved3.getEmployees().size());
        }

        @Test
        @DisplayName("Should handle concurrent reads and writes")
        void cacheService_ShouldHandleConcurrency_WhenConcurrentReadsAndWrites() throws InterruptedException {
            // Arrange
            String key = KEY_PREFIX + "concurrent:person";
            Person person = createTestPerson(1L, "Concurrent User");

            // Act - Write
            cacheService.put(key, person);

            // Multiple concurrent reads
            Thread[] threads = new Thread[5];
            Person[] results = new Person[5];

            for (int i = 0; i < 5; i++) {
                final int index = i;
                threads[i] = new Thread(() -> results[index] = cacheService.get(key, Person.class));
                threads[i].start();
            }

            // Wait for all threads
            for (Thread thread : threads) {
                thread.join();
            }

            // Assertions - All reads should return the same data
            for (Person result : results) {
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
        void delete_ShouldDeleteKey_WhenCachedObject() {
            // Arrange
            String key = KEY_PREFIX + "delete:person";
            Person person = createTestPerson(1L, "Delete User");
            cacheService.put(key, person);

            // Act
            Boolean deleted = cacheService.delete(key);

            // Assertions
            assertTrue(deleted);
            assertFalse(cacheService.hasKey(key));
        }

        @Test
        @DisplayName("Should check if key exists")
        void hasKey_ShouldReturnFalseTrue_WhenKeyNotExistsAndExists() {
            // Arrange
            String key = KEY_PREFIX + "exists:person";
            Person person = createTestPerson(1L, "Exists User");

            // Act - Before setting
            boolean existsBefore = cacheService.hasKey(key);

            cacheService.put(key, person);

            // Act - After setting
            boolean existsAfter = cacheService.hasKey(key);

            // Assertions
            assertFalse(existsBefore);
            assertTrue(existsAfter);
        }
    }

    // ==================== CacheService hasKey Tests ====================

    @Nested
    @DisplayName("CacheService hasKey Operations")
    class HasKeyTests {

        @Test
        @DisplayName("Should return false for blank key")
        void hasKey_ShouldReturnFalse_WhenKeyIsBlank() {
            assertFalse(cacheService.hasKey(""));
            assertFalse(cacheService.hasKey("   "));
        }

        @Test
        @DisplayName("Should return false for null key")
        void hasKey_ShouldReturnFalse_WhenKeyIsNull() {
            assertFalse(cacheService.hasKey(null));
        }

        @Test
        @DisplayName("Should return false for non-existent key")
        void hasKey_ShouldReturnFalse_WhenKeyDoesNotExist() {
            assertFalse(cacheService.hasKey(KEY_PREFIX + "nonexistent:key"));
        }

        @Test
        @DisplayName("Should return true for existing key")
        void hasKey_ShouldReturnTrue_WhenKeyExists() {
            // Arrange
            String key = KEY_PREFIX + "haskey:exists";
            cacheService.put(key, createTestPerson(1L, "Exists"));

            // Assert
            assertTrue(cacheService.hasKey(key));
        }
    }

    // ==================== CacheService put Tests ====================

    @Nested
    @DisplayName("CacheService put Operations")
    class PutTests {

        @Test
        @DisplayName("Should not store null value")
        void put_ShouldNotStore_WhenValueIsNull() {
            // Arrange
            String key = KEY_PREFIX + "put:null";

            // Act
            cacheService.put(key, null);

            // Assert
            assertFalse(cacheService.hasKey(key));
        }

        @Test
        @DisplayName("Should not store null value with TTL")
        void put_ShouldNotStore_WhenValueIsNullWithTTL() {
            // Arrange
            String key = KEY_PREFIX + "put:null:ttl";

            // Act
            cacheService.put(key, null, 1, TimeUnit.HOURS);

            // Assert
            assertFalse(cacheService.hasKey(key));
        }

        @Test
        @DisplayName("Should store Person value")
        void put_ShouldStore_WhenPersonValue() {
            // Arrange
            String key = KEY_PREFIX + "put:person";

            // Act
            Person person = createTestPerson(1L, "Store Test");
            cacheService.put(key, person);
            Person result = cacheService.get(key, Person.class);

            // Assert
            assertNotNull(result);
            assertEquals("Store Test", result.getName());
        }

        @Test
        @DisplayName("Should store Address value")
        void put_ShouldStore_WhenAddressValue() {
            // Arrange
            String key = KEY_PREFIX + "put:address";

            // Act
            Address address = createTestAddress(1L, "Istanbul");
            cacheService.put(key, address);
            Address result = cacheService.get(key, Address.class);

            // Assert
            assertNotNull(result);
            assertEquals("Istanbul", result.getCity());
        }

        @Test
        @DisplayName("Should overwrite existing value")
        void put_ShouldOverwrite_WhenKeyAlreadyExists() {
            // Arrange
            String key = KEY_PREFIX + "put:overwrite";
            cacheService.put(key, createTestPerson(1L, "Original"));

            // Act
            cacheService.put(key, createTestPerson(1L, "Updated"));
            Person result = cacheService.get(key, Person.class);

            // Assert
            assertEquals("Updated", result.getName());
        }
    }

    // ==================== CacheService get with type Tests ====================

    @Nested
    @DisplayName("CacheService get(key, class) Operations")
    class GetWithTypeTests {

        @Test
        @DisplayName("Should return null when key does not exist")
        void get_ShouldReturnNull_WhenKeyDoesNotExist() {
            // Act
            Person result = cacheService.get(KEY_PREFIX + "get:nonexistent", Person.class);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should return typed object")
        void get_ShouldReturnTypedObject_WhenKeyExists() {
            // Arrange
            String key = KEY_PREFIX + "get:typed";
            Person person = createTestPerson(1L, "Typed User");
            cacheService.put(key, person);

            // Act
            Person result = cacheService.get(key, Person.class);

            // Assert
            assertNotNull(result);
            assertEquals("Typed User", result.getName());
            assertEquals(1L, result.getId());
        }
    }

    // ==================== CacheService get collection Tests ====================

    @Nested
    @DisplayName("CacheService get(key, collectionType, elementType) Operations")
    class GetCollectionTests {

        @Test
        @DisplayName("Should return empty list when key does not exist")
        void get_ShouldReturnEmptyList_WhenKeyDoesNotExist() {
            // Act
            Collection<Person> result = cacheService.get(KEY_PREFIX + "collection:nonexistent", List.class, Person.class);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return List when List.class is specified")
        void get_ShouldReturnList_WhenListClassSpecified() {
            // Arrange
            String key = KEY_PREFIX + "collection:list";
            List<Person> persons = new ArrayList<>();
            persons.add(createTestPerson(1L, "Person 1"));
            persons.add(createTestPerson(2L, "Person 2"));
            cacheService.put(key, persons);

            // Act
            Collection<Person> result = cacheService.get(key, List.class, Person.class);

            // Assert
            assertNotNull(result);
            assertInstanceOf(List.class, result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return Set when Set.class is specified")
        void get_ShouldReturnSet_WhenSetClassSpecified() {
            // Arrange
            String key = KEY_PREFIX + "collection:set";
            List<Person> persons = new ArrayList<>();
            persons.add(createTestPerson(1L, "Person 1"));
            persons.add(createTestPerson(2L, "Person 2"));
            cacheService.put(key, persons);

            // Act
            Collection<Person> result = cacheService.get(key, Set.class, Person.class);

            // Assert
            assertNotNull(result);
            assertInstanceOf(Set.class, result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return List for unrecognized collection type")
        void get_ShouldReturnList_WhenUnrecognizedCollectionType() {
            // Arrange
            String key = KEY_PREFIX + "collection:default";
            List<Person> persons = new ArrayList<>();
            persons.add(createTestPerson(1L, "Person 1"));
            cacheService.put(key, persons);

            // Act
            Collection<Person> result = cacheService.get(key, Collection.class, Person.class);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should properly convert element types in list")
        void get_ShouldConvertElementTypes_WhenListOfAddresses() {
            // Arrange
            String key = KEY_PREFIX + "collection:addresses";
            List<Address> addresses = new ArrayList<>();
            addresses.add(createTestAddress(1L, "Istanbul"));
            addresses.add(createTestAddress(2L, "Ankara"));
            cacheService.put(key, addresses);

            // Act
            List<Address> result = (List<Address>) cacheService.get(key, List.class, Address.class);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Istanbul", result.get(0).getCity());
            assertEquals("Ankara", result.get(1).getCity());
        }
    }

    // ==================== CacheService getMap Tests ====================

    @Nested
    @DisplayName("CacheService getMap Operations")
    class GetMapTests {

        @Test
        @DisplayName("Should return empty map when key does not exist")
        void getMap_ShouldReturnEmptyMap_WhenKeyDoesNotExist() {
            // Act
            Map<String, Person> result = cacheService.getMap(KEY_PREFIX + "map:nonexistent", String.class, Person.class);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return typed map with Person values")
        void getMap_ShouldReturnTypedMap_WhenMapWithPersonValues() {
            // Arrange
            String key = KEY_PREFIX + "map:persons";
            Map<String, Person> personMap = new HashMap<>();
            personMap.put("p1", createTestPerson(1L, "Alice"));
            personMap.put("p2", createTestPerson(2L, "Bob"));
            cacheService.put(key, personMap);

            // Act
            Map<String, Person> result = cacheService.getMap(key, String.class, Person.class);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertInstanceOf(Person.class, result.get("p1"));
            assertEquals("Alice", result.get("p1").getName());
            assertEquals("Bob", result.get("p2").getName());
        }

        @Test
        @DisplayName("Should return typed map with Address values")
        void getMap_ShouldReturnTypedMap_WhenMapWithAddressValues() {
            // Arrange
            String key = KEY_PREFIX + "map:addresses";
            Map<String, Address> addressMap = new HashMap<>();
            addressMap.put("hq", createTestAddress(1L, "Istanbul"));
            addressMap.put("branch", createTestAddress(2L, "Ankara"));
            cacheService.put(key, addressMap);

            // Act
            Map<String, Address> result = cacheService.getMap(key, String.class, Address.class);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertInstanceOf(Address.class, result.get("hq"));
            assertEquals("Istanbul", result.get("hq").getCity());
        }

        @Test
        @DisplayName("Should return typed map with String values")
        void getMap_ShouldReturnTypedMap_WhenMapWithStringValues() {
            // Arrange
            String key = KEY_PREFIX + "map:strings";
            Map<String, String> stringMap = new HashMap<>();
            stringMap.put("lang", "Java");
            stringMap.put("framework", "Spring");
            cacheService.put(key, stringMap);

            // Act
            Map<String, String> result = cacheService.getMap(key, String.class, String.class);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Java", result.get("lang"));
            assertEquals("Spring", result.get("framework"));
        }

        @Test
        @DisplayName("Should return empty map when stored map is empty")
        void getMap_ShouldReturnEmptyMap_WhenStoredMapIsEmpty() {
            // Arrange
            String key = KEY_PREFIX + "map:empty";
            cacheService.put(key, new HashMap<>());

            // Act
            Map<String, Person> result = cacheService.getMap(key, String.class, Person.class);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== CacheService getKeys Tests ====================

    @Nested
    @DisplayName("CacheService getKeys Operations")
    class GetKeysTests {

        @Test
        @DisplayName("Should return matching keys")
        void getKeys_ShouldReturnKeys_WhenKeysMatchPattern() {
            // Arrange
            cacheService.put(KEY_PREFIX + "keys:a", createTestPerson(1L, "A"));
            cacheService.put(KEY_PREFIX + "keys:b", createTestPerson(2L, "B"));
            cacheService.put(KEY_PREFIX + "keys:c", createTestPerson(3L, "C"));

            // Act
            Set<String> keys = cacheService.getKeys(KEY_PREFIX + "keys:*");

            // Assert
            assertNotNull(keys);
            assertEquals(3, keys.size());
        }

        @Test
        @DisplayName("Should return empty set when no keys match")
        void getKeys_ShouldReturnEmptySet_WhenNoKeysMatch() {
            // Act
            Set<String> keys = cacheService.getKeys(KEY_PREFIX + "nonexistent:pattern:*");

            // Assert
            assertNotNull(keys);
            assertTrue(keys.isEmpty());
        }
    }

    // ==================== CacheService delete Tests ====================

    @Nested
    @DisplayName("CacheService delete and deleteAll Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete existing key")
        void delete_ShouldReturnTrue_WhenKeyExists() {
            // Arrange
            String key = KEY_PREFIX + "delete:single";
            cacheService.put(key, createTestPerson(1L, "Delete Me"));

            // Act
            Boolean result = cacheService.delete(key);

            // Assert
            assertTrue(result);
            assertFalse(cacheService.hasKey(key));
        }

        @Test
        @DisplayName("Should return false when deleting non-existent key")
        void delete_ShouldReturnFalse_WhenKeyDoesNotExist() {
            // Act
            Boolean result = cacheService.delete(KEY_PREFIX + "delete:nonexistent");

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should delete all keys in set")
        void deleteAll_ShouldDeleteAllKeys_WhenMultipleKeys() {
            // Arrange
            cacheService.put(KEY_PREFIX + "deleteall:1", createTestPerson(1L, "D1"));
            cacheService.put(KEY_PREFIX + "deleteall:2", createTestPerson(2L, "D2"));
            cacheService.put(KEY_PREFIX + "deleteall:3", createTestPerson(3L, "D3"));

            Set<String> keysToDelete = new HashSet<>();
            keysToDelete.add(KEY_PREFIX + "deleteall:1");
            keysToDelete.add(KEY_PREFIX + "deleteall:2");
            keysToDelete.add(KEY_PREFIX + "deleteall:3");

            // Act
            boolean result = cacheService.deleteAll(keysToDelete);

            // Assert
            assertTrue(result);
            assertFalse(cacheService.hasKey(KEY_PREFIX + "deleteall:1"));
            assertFalse(cacheService.hasKey(KEY_PREFIX + "deleteall:2"));
            assertFalse(cacheService.hasKey(KEY_PREFIX + "deleteall:3"));
        }

        @Test
        @DisplayName("Should return false when not all keys deleted")
        void deleteAll_ShouldReturnFalse_WhenNotAllKeysExist() {
            // Arrange
            cacheService.put(KEY_PREFIX + "deleteall:existing", createTestPerson(1L, "Existing"));

            Set<String> keysToDelete = new HashSet<>();
            keysToDelete.add(KEY_PREFIX + "deleteall:existing");
            keysToDelete.add(KEY_PREFIX + "deleteall:missing");

            // Act
            boolean result = cacheService.deleteAll(keysToDelete);

            // Assert
            assertFalse(result); // Not all keys were deleted
        }
    }

    // ==================== CacheService getHashOps Tests ====================

    @Nested
    @DisplayName("CacheService HashOperations")
    class HashOpsTests {

        @Test
        @DisplayName("Should return HashOperations")
        void getHashOps_ShouldReturnHashOperations() {
            // Act
            var hashOps = cacheService.getHashOps();

            // Assert
            assertNotNull(hashOps);
        }
    }

    // ==================== CacheService TTL Tests ====================

    @Nested
    @DisplayName("CacheService TTL Operations")
    class TTLTests {

        @Test
        @DisplayName("Should store with TTL and key should exist")
        void put_ShouldStoreWithTTL_WhenValidTTL() {
            // Arrange
            String key = KEY_PREFIX + "ttl:valid";
            Person person = createTestPerson(1L, "TTL Person");

            // Act
            cacheService.put(key, person, 60, TimeUnit.SECONDS);

            // Assert
            assertTrue(cacheService.hasKey(key));
            Person result = cacheService.get(key, Person.class);
            assertNotNull(result);
            assertEquals("TTL Person", result.getName());
        }

        @Test
        @DisplayName("Should store list with TTL")
        void put_ShouldStoreListWithTTL_WhenListValue() {
            // Arrange
            String key = KEY_PREFIX + "ttl:list";
            List<Person> persons = new ArrayList<>();
            persons.add(createTestPerson(1L, "TTL Person 1"));
            persons.add(createTestPerson(2L, "TTL Person 2"));

            // Act
            cacheService.put(key, persons, 60, TimeUnit.SECONDS);

            // Assert
            assertTrue(cacheService.hasKey(key));
            List<Person> result = (List<Person>) cacheService.get(key, List.class, Person.class);
            assertEquals(2, result.size());
        }
    }

    // ==================== Complex Scenario Tests ====================

    @Nested
    @DisplayName("Complex Scenario Redis Operations")
    class ComplexScenarioTests {

        @Test
        @DisplayName("Should handle map with nested objects via getMap")
        void getMap_ShouldHandleNestedObjects_WhenMapWithCompanyValues() {
            // Arrange
            String key = KEY_PREFIX + "complex:company:map";
            Map<String, Company> companyMap = new HashMap<>();
            companyMap.put("company1", createTestCompany());

            // Act
            cacheService.put(key, companyMap);
            Map<String, Company> result = cacheService.getMap(key, String.class, Company.class);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertInstanceOf(Company.class, result.get("company1"));
            assertEquals("Test Company", result.get("company1").getName());
        }

        @Test
        @DisplayName("Should handle multiple put and get cycles for same key")
        void cacheService_ShouldHandleMultiplePutGet_WhenSameKey() {
            // Arrange
            String key = KEY_PREFIX + "complex:multiput";

            // Act - store person
            Person person = createTestPerson(1L, "First");
            cacheService.put(key, person);
            Person result1 = cacheService.get(key, Person.class);
            assertEquals("First", result1.getName());

            // Act - overwrite with address
            Address address = createTestAddress(1L, "Istanbul");
            cacheService.put(key, address);
            Address result2 = cacheService.get(key, Address.class);
            assertEquals("Istanbul", result2.getCity());
        }

        @Test
        @DisplayName("Should store and retrieve department via get with type")
        void get_ShouldReturnDepartment_WhenStoredDepartment() {
            // Arrange
            String key = KEY_PREFIX + "complex:department";
            Department dept = createTestDepartment();
            cacheService.put(key, dept);

            // Act
            Department result = cacheService.get(key, Department.class);

            // Assert
            assertNotNull(result);
            assertEquals("Engineering", result.getName());
            assertNotNull(result.getTeamGroups());
            assertNotNull(result.getTeamMembers());
            assertNotNull(result.getConfigurations());
        }
    }
}
