package com.mb.livedataservice.config.serializer;

import com.mb.livedataservice.data.model.redis.dto.TestAddressDto;
import com.mb.livedataservice.data.model.redis.dto.TestCompanyDto;
import com.mb.livedataservice.data.model.redis.dto.TestDepartmentDto;
import com.mb.livedataservice.data.model.redis.dto.TestPersonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for CustomJackson2JsonRedisSerializer.
 * Tests all cacheable structures: list, object, map, set, nested objects, and nested collections.
 */
@DisplayName("CustomJackson2JsonRedisSerializer Integration Tests")
class CustomJackson2JsonRedisSerializerTest {

    private CustomJackson2JsonRedisSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new CustomJackson2JsonRedisSerializer();
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
    @DisplayName("Single Object Serialization Tests")
    class SingleObjectTests {

        @Test
        void serialize_ShouldSerializeAndDeserializeSimpleObject_WhenValidObjectProvided() {
            // Arrange
            TestPersonDto person = createTestPerson(1L, "John Doe");

            // Act
            byte[] serialized = serializer.serialize(person);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(serialized);
            assertNotNull(deserialized);
            assertInstanceOf(TestPersonDto.class, deserialized);

            TestPersonDto result = (TestPersonDto) deserialized;
            assertEquals(person.getId(), result.getId());
            assertEquals(person.getName(), result.getName());
            assertEquals(person.getEmail(), result.getEmail());
            assertEquals(person.getAge(), result.getAge());
            assertEquals(person.getActive(), result.getActive());
            assertEquals(person.getBirthDate(), result.getBirthDate());
            assertEquals(person.getCreatedAt(), result.getCreatedAt());
        }

        @Test
        void serialize_ShouldSerializeAndDeserializeObjectWithNestedObject_WhenNestedObjectProvided() {
            // Arrange
            TestCompanyDto company = TestCompanyDto.builder()
                    .id(1L)
                    .name("Nested Test Company")
                    .industry("Tech")
                    .headquarters(createTestAddress(1L, "Istanbul"))
                    .build();

            // Act
            byte[] serialized = serializer.serialize(company);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(TestCompanyDto.class, deserialized);

            TestCompanyDto result = (TestCompanyDto) deserialized;
            assertEquals(company.getId(), result.getId());
            assertEquals(company.getName(), result.getName());
            assertNotNull(result.getHeadquarters());
            assertEquals(company.getHeadquarters().getCity(), result.getHeadquarters().getCity());
        }

        @Test
        void serialize_ShouldHandleNullValues_WhenNullProvided() {
            // Act
            byte[] serialized = serializer.serialize(null);
            Object deserialized = serializer.deserialize(null);

            // Assertions
            assertEquals(0, serialized.length);
            assertNull(deserialized);
        }

        @Test
        void deserialize_ShouldHandleEmptyByteArray_WhenEmptyByteArrayProvided() {
            // Act
            Object deserialized = serializer.deserialize(new byte[0]);

            // Assertions
            assertNull(deserialized);
        }
    }

    // ==================== List Tests ====================

    @Nested
    @DisplayName("List Serialization Tests")
    class ListTests {

        @Test
        void serialize_ShouldSerializeAndDeserializeListOfObjects_WhenListOfObjectsProvided() {
            // Arrange
            List<TestPersonDto> persons = new ArrayList<>();
            persons.add(createTestPerson(1L, "John Doe"));
            persons.add(createTestPerson(2L, "Jane Smith"));
            persons.add(createTestPerson(3L, "Bob Wilson"));

            // Act
            byte[] serialized = serializer.serialize(persons);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(List.class, deserialized);

            @SuppressWarnings("unchecked")
            List<TestPersonDto> result = (List<TestPersonDto>) deserialized;
            assertEquals(3, result.size());
            assertEquals("John Doe", result.get(0).getName());
            assertEquals("Jane Smith", result.get(1).getName());
            assertEquals("Bob Wilson", result.get(2).getName());
        }

        @Test
        void serialize_ShouldSerializeAndDeserializeEmptyList_WhenEmptyListProvided() {
            // Arrange
            List<TestPersonDto> emptyList = new ArrayList<>();

            // Act
            byte[] serialized = serializer.serialize(emptyList);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(List.class, deserialized);

            @SuppressWarnings("unchecked")
            List<TestPersonDto> result = (List<TestPersonDto>) deserialized;
            assertTrue(result.isEmpty());
        }

        @Test
        void serialize_ShouldSerializeAndDeserializeListWithNestedObjects_WhenListWithNestedObjectsProvided() {
            // Arrange
            List<TestCompanyDto> companies = new ArrayList<>();
            companies.add(createTestCompany());

            // Act
            byte[] serialized = serializer.serialize(companies);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(List.class, deserialized);

            @SuppressWarnings("unchecked")
            List<TestCompanyDto> result = (List<TestCompanyDto>) deserialized;
            assertEquals(1, result.size());
            assertNotNull(result.getFirst().getHeadquarters());
            assertNotNull(result.getFirst().getEmployees());
        }
    }

    // ==================== Set Tests ====================

    @Nested
    @DisplayName("Set Serialization Tests")
    class SetTests {

        @Test
        void serialize_ShouldSerializeAndDeserializeSetOfObjects_WhenSetOfObjectsProvided() {
            // Arrange
            Set<TestPersonDto> persons = new HashSet<>();
            persons.add(createTestPerson(1L, "John Doe"));
            persons.add(createTestPerson(2L, "Jane Smith"));

            // Act
            byte[] serialized = serializer.serialize(persons);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            // Note: Sets are deserialized as Lists by default
            assertInstanceOf(List.class, deserialized);

            @SuppressWarnings("unchecked")
            List<TestPersonDto> result = (List<TestPersonDto>) deserialized;
            assertEquals(2, result.size());
        }

        @Test
        void serialize_ShouldSerializeAndDeserializeLinkedHashSet_WhenLinkedHashSetProvided() {
            // Arrange
            Set<TestAddressDto> addresses = new LinkedHashSet<>();
            addresses.add(createTestAddress(1L, "Istanbul"));
            addresses.add(createTestAddress(2L, "Ankara"));
            addresses.add(createTestAddress(3L, "Izmir"));

            // Act
            byte[] serialized = serializer.serialize(addresses);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(List.class, deserialized);

            @SuppressWarnings("unchecked")
            List<TestAddressDto> result = (List<TestAddressDto>) deserialized;
            assertEquals(3, result.size());
        }
    }

    // ==================== Map Tests ====================

    @Nested
    @DisplayName("Map Serialization Tests")
    class MapTests {

        @Test
        void serialize_ShouldSerializeAndDeserializeMapWithObjectValues_WhenMapWithStringKeysAndObjectValuesProvided() {
            // Arrange
            Map<String, TestPersonDto> personMap = new HashMap<>();
            personMap.put("person1", createTestPerson(1L, "John Doe"));
            personMap.put("person2", createTestPerson(2L, "Jane Smith"));

            // Act
            byte[] serialized = serializer.serialize(personMap);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(Map.class, deserialized);

            @SuppressWarnings("unchecked")
            Map<String, TestPersonDto> result = (Map<String, TestPersonDto>) deserialized;
            // Map should have exactly 2 entries (no @class key)
            assertEquals(2, result.size());
            assertTrue(result.containsKey("person1"));
            assertTrue(result.containsKey("person2"));
            // Verify @class is not a key in the map
            assertFalse(result.containsKey("@class"));
        }

        @Test
        void serialize_ShouldSerializeAndDeserializeEmptyMap_WhenEmptyMapProvided() {
            // Arrange
            Map<String, TestPersonDto> emptyMap = new HashMap<>();

            // Act
            byte[] serialized = serializer.serialize(emptyMap);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(Map.class, deserialized);

            @SuppressWarnings("unchecked")
            Map<String, TestPersonDto> result = (Map<String, TestPersonDto>) deserialized;
            assertTrue(result.isEmpty());
        }

        @Test
        void serialize_ShouldSerializeAndDeserializeMapWithNestedCollections_WhenMapWithNestedCollectionsProvided() {
            // Arrange
            Map<String, List<TestPersonDto>> teamMap = new HashMap<>();
            teamMap.put("team1", List.of(createTestPerson(1L, "Dev 1"), createTestPerson(2L, "Dev 2")));
            teamMap.put("team2", List.of(createTestPerson(3L, "Dev 3")));

            // Act
            byte[] serialized = serializer.serialize(teamMap);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(Map.class, deserialized);

            @SuppressWarnings("unchecked")
            Map<String, List<TestPersonDto>> result = (Map<String, List<TestPersonDto>>) deserialized;
            // Map should have exactly 2 entries (no @class key)
            assertEquals(2, result.size());
            assertTrue(result.containsKey("team1"));
            assertTrue(result.containsKey("team2"));
            assertFalse(result.containsKey("@class"));

            // Verify the nested lists exist and have correct sizes
            assertInstanceOf(List.class, result.get("team1"));
            assertInstanceOf(List.class, result.get("team2"));

            List<TestPersonDto> team1 = result.get("team1");
            List<TestPersonDto> team2 = result.get("team2");
            assertEquals(2, team1.size());
            assertEquals(1, team2.size());
        }
    }

    // ==================== Nested Object Tests ====================

    @Nested
    @DisplayName("Nested Object Serialization Tests")
    class NestedObjectTests {

        @Test
        void serialize_ShouldSerializeAndDeserializeObjectWithMultipleLevelsOfNesting_WhenObjectWithMultipleLevelsOfNestingProvided() {
            // Arrange
            TestCompanyDto company = createTestCompany();

            // Act
            byte[] serialized = serializer.serialize(company);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(TestCompanyDto.class, deserialized);

            TestCompanyDto result = (TestCompanyDto) deserialized;
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
        void serialize_ShouldSerializeAndDeserializeObjectWithNullNestedFields_WhenObjectWithNullNestedFieldsProvided() {
            // Arrange
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

            // Act
            byte[] serialized = serializer.serialize(company);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(TestCompanyDto.class, deserialized);

            TestCompanyDto result = (TestCompanyDto) deserialized;
            assertEquals(company.getId(), result.getId());
            assertNull(result.getHeadquarters());
            assertNull(result.getEmployees());
        }
    }

    // ==================== Nested Collections Tests ====================

    @Nested
    @DisplayName("Nested Collections Serialization Tests")
    class NestedCollectionsTests {

        @Test
        void serialize_ShouldSerializeAndDeserializeDeeplyNestedCollections_WhenDeeplyNestedCollectionsProvided() {
            // Arrange
            TestDepartmentDto department = createTestDepartment();

            // Act
            byte[] serialized = serializer.serialize(department);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(TestDepartmentDto.class, deserialized);

            TestDepartmentDto result = (TestDepartmentDto) deserialized;
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
        void serialize_ShouldSerializeAndDeserializeListOfDepartmentsWithNestedCollections_WhenListOfDepartmentsWithNestedCollectionsProvided() {
            // Arrange
            List<TestDepartmentDto> departments = new ArrayList<>();
            departments.add(createTestDepartment());
            departments.add(TestDepartmentDto.builder()
                    .id(2L)
                    .name("Marketing")
                    .teamGroups(List.of(List.of("Campaign Team")))
                    .build());

            // Act
            byte[] serialized = serializer.serialize(departments);
            Object deserialized = serializer.deserialize(serialized);

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(List.class, deserialized);

            @SuppressWarnings("unchecked")
            List<TestDepartmentDto> result = (List<TestDepartmentDto>) deserialized;
            assertEquals(2, result.size());
            assertEquals("Engineering", result.get(0).getName());
            assertEquals("Marketing", result.get(1).getName());
        }
    }

    // ==================== Legacy Format Compatibility Tests ====================

    @Nested
    @DisplayName("Legacy Format Compatibility Tests")
    class LegacyFormatTests {

        @Test
        void deserialize_ShouldDeserializeLegacyFormatWithClassProperty_WhenLegacyFormatWithClassPropertyProvided() {
            // Arrange - Legacy format: [{"@class":"...", "id":1, ...}]
            String legacyJson = "[{\"@class\":\"com.mb.livedataservice.data.model.redis.dto.TestPersonDto\"," +
                    "\"id\":1,\"name\":\"Legacy User\",\"email\":\"legacy@test.com\"," +
                    "\"age\":25,\"active\":true}]";

            // Act
            Object deserialized = serializer.deserialize(legacyJson.getBytes());

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(List.class, deserialized);

            @SuppressWarnings("unchecked")
            List<TestPersonDto> result = (List<TestPersonDto>) deserialized;
            assertEquals(1, result.size());
            assertEquals("Legacy User", result.getFirst().getName());
            assertEquals(1L, result.getFirst().getId());
        }

        @Test
        void deserialize_ShouldDeserializeSingleObjectWithClassProperty_WhenSingleObjectWithClassPropertyProvided() {
            // Arrange
            String legacyJson = "{\"@class\":\"com.mb.livedataservice.data.model.redis.dto.TestAddressDto\"," +
                    "\"id\":1,\"street\":\"Test Street\",\"city\":\"Istanbul\"," +
                    "\"country\":\"Turkey\",\"zipCode\":\"34000\"}";

            // Act
            Object deserialized = serializer.deserialize(legacyJson.getBytes());

            // Assertions
            assertNotNull(deserialized);
            assertInstanceOf(TestAddressDto.class, deserialized);

            TestAddressDto result = (TestAddressDto) deserialized;
            assertEquals("Istanbul", result.getCity());
            assertEquals("Test Street", result.getStreet());
        }
    }

    // ==================== Round-Trip Tests ====================

    @Nested
    @DisplayName("Round-Trip Serialization Tests")
    class RoundTripTests {

        @Test
        void serialize_ShouldMaintainDataIntegrityThroughMultipleCycles_WhenMultipleSerializeDeserializeCyclesPerformed() {
            // Arrange
            TestCompanyDto original = createTestCompany();

            // Act - First cycle
            byte[] serialized1 = serializer.serialize(original);
            TestCompanyDto deserialized1 = (TestCompanyDto) serializer.deserialize(serialized1);

            // Second cycle
            byte[] serialized2 = serializer.serialize(deserialized1);
            TestCompanyDto deserialized2 = (TestCompanyDto) serializer.deserialize(serialized2);

            // Third cycle
            byte[] serialized3 = serializer.serialize(deserialized2);
            TestCompanyDto deserialized3 = (TestCompanyDto) serializer.deserialize(serialized3);

            // Assertions
            assertNotNull(deserialized3);
            assertEquals(original.getId(), deserialized3.getId());
            assertEquals(original.getName(), deserialized3.getName());
            assertEquals(original.getHeadquarters().getCity(), deserialized3.getHeadquarters().getCity());
            assertEquals(original.getEmployees().size(), deserialized3.getEmployees().size());
        }

        @Test
        void serialize_ShouldProduceConsistentSerializationOutput_WhenSameObjectSerializedMultipleTimes() {
            // Arrange
            TestPersonDto person = createTestPerson(1L, "Consistent User");

            // Act
            byte[] serialized1 = serializer.serialize(person);
            byte[] serialized2 = serializer.serialize(person);

            // Assertions
            assertArrayEquals(serialized1, serialized2);
        }
    }
}
