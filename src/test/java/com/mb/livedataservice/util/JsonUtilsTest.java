package com.mb.livedataservice.util;

import com.mb.livedataservice.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.node.ArrayNode;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JSONUtils Tests")
class JsonUtilsTest {

    private static final String SINGLE_JSON_OBJECT = """
            {
                "name": "John",
                "age": 30
            }
            """;

    private static final String VALID_JSON_ARRAY = """
            [
                {
                    "name": "John",
                    "age": 30
                },
                {
                    "name": "Jane",
                    "age": 25
                }
            ]
            """;

    private static final String INVALID_JSON_ARRAY = """
            [
              {
                "name": "Alice",
                "age": "thirty"
              }
            ]
            """;

    private static final String INVALID_JSON_ARRAY_FORMAT = """
            {
              "name": "Alice",
              "age": 30
            }
            """;

    private static final String STRING_JSON_ARRAY = """
            [
                "test1",
                "test2"
            ]
            """;

    private static final String INVALID_JSON_WITH_MISSING_QUOTES = """
            {
                name: John
            }
            """;

    private static final String MALFORMED_JSON_WITH_INVALID_FORMAT = """
            {
                name: 'John',
                age: 30,
                city: 'New York'
            }
            """;

    private static final String VALID_JSON_OBJECT = """
            {
                "key": "value"
            }
            """;


    private static final String INVALID_JSON_OBJECT = """
            {
                "key": "value"
                ,
            }
            """;

    private static final String INVALID_JSON_OBJECT_WITH_TRAILING_TOKENS = """
            {
                "key": "value",
            } extra
            """;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Person {
        private String name;
        private int age;
    }

    @Nested
    @DisplayName("Object Mapper Tests")
    class ObjectMapperTests {

        @Test
        void createMapper_ShouldReturnConfiguredObjectMapper_WhenCalled() {
            // Arrange
            // Act
            ObjectMapper mapper = JsonUtils.createMapper();

            // Assertions
            assertNotNull(mapper);
            assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
            assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES));
        }

        @Test
        void getMapper_ShouldReturnSameInstance_WhenCalledMultipleTimes() {
            // Arrange
            // Act
            ObjectMapper firstCall = JsonUtils.getMapper();
            ObjectMapper secondCall = JsonUtils.getMapper();

            // Assertions
            assertNotNull(firstCall);
            assertSame(firstCall, secondCall);
        }
    }

    @Nested
    @DisplayName("Serialize Tests")
    class SerializeTests {

        @Test
        void serialize_ShouldReturnJsonString_WhenValidObjectProvided() {
            // Arrange
            Person person = new Person("John", 30);

            // Act
            String json = JsonUtils.serialize(person);

            // Assertions
            assertNotNull(json);
            assertTrue(json.contains("\"name\":\"John\""));
            assertTrue(json.contains("\"age\":30"));
        }

        @Test
        void serialize_ShouldReturnNull_WhenNullObjectProvided() {
            // Arrange
            // Act
            String json = JsonUtils.serialize(null);

            // Assertions
            assertNotNull(json);
            assertEquals("null", json);
        }

        @Test
        void serialize_ShouldSerializeList_WhenListProvided() {
            // Arrange
            List<Person> people = List.of(
                    new Person("John", 30),
                    new Person("Jane", 25)
            );

            // Act
            String json = JsonUtils.serialize(people);

            // Assertions
            assertNotNull(json);
            assertTrue(json.contains("John"));
            assertTrue(json.contains("Jane"));
        }

        @Test
        void serialize_ShouldSerializeMap_WhenMapProvided() {
            // Arrange
            Map<String, Object> map = Map.of("key1", "value1", "key2", 42);

            // Act
            String json = JsonUtils.serialize(map);

            // Assertions
            assertNotNull(json);
            assertTrue(json.contains("\"key1\":\"value1\""));
            assertTrue(json.contains("\"key2\":42"));
        }

        @Test
        void serialize_ShouldSerializeEmptyList_WhenEmptyListProvided() {
            // Arrange
            List<Person> emptyList = List.of();

            // Act
            String json = JsonUtils.serialize(emptyList);

            // Assertions
            assertNotNull(json);
            assertEquals("[]", json);
        }

        @Test
        void serialize_ShouldSerializeEmptyMap_WhenEmptyMapProvided() {
            // Arrange
            Map<String, Object> emptyMap = Map.of();

            // Act
            String json = JsonUtils.serialize(emptyMap);

            // Assertions
            assertNotNull(json);
            assertEquals("{}", json);
        }

        @Test
        void serialize_ShouldSerializeString_WhenStringProvided() {
            // Arrange
            String text = "test string";

            // Act
            String json = JsonUtils.serialize(text);

            // Assertions
            assertNotNull(json);
            assertEquals("\"test string\"", json);
        }

        @Test
        void serialize_ShouldSerializeNumber_WhenNumberProvided() {
            // Arrange
            Integer number = 42;

            // Act
            String json = JsonUtils.serialize(number);

            // Assertions
            assertNotNull(json);
            assertEquals("42", json);
        }

        @Test
        void serialize_ShouldSerializeBoolean_WhenBooleanProvided() {
            // Arrange
            Boolean flag = true;

            // Act
            String json = JsonUtils.serialize(flag);

            // Assertions
            assertNotNull(json);
            assertEquals("true", json);
        }
    }

    @Nested
    @DisplayName("Deserialize Tests")
    class DeserializeTests {

        @Test
        void deserialize_ShouldReturnObject_WhenValidJsonAndClassProvided() {
            // Arrange
            // Act
            Person person = JsonUtils.deserialize(SINGLE_JSON_OBJECT, Person.class);

            // Assertions
            assertNotNull(person);
            assertEquals("John", person.getName());
            assertEquals(30, person.getAge());
        }

        @Test
        void deserialize_ShouldReturnNull_WhenNullJsonProvided() {
            // Arrange
            // Act
            Person person = JsonUtils.deserialize("null", Person.class);

            // Assertions
            assertNull(person);
        }

        @Test
        void deserialize_ShouldReturnString_WhenStringClassProvided() {
            // Arrange
            String content = "test string";

            // Act
            String result = JsonUtils.deserialize(content, String.class);

            // Assertions
            assertEquals("test string", result);
        }

        @Test
        void deserialize_ShouldThrowException_WhenInvalidJsonProvided() {
            // Arrange
            // Act
            // Assertions
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.deserialize(INVALID_JSON_WITH_MISSING_QUOTES, Person.class));
            assertNotNull(exception);
        }

        @Test
        void deserialize_ShouldReturnObject_WhenValidJsonAndTypeReferenceProvided() {
            // Arrange
            TypeReference<Person> typeRef = new TypeReference<>() {
            };

            // Act
            Person person = JsonUtils.deserialize(SINGLE_JSON_OBJECT, typeRef);

            // Assertions
            assertNotNull(person);
            assertEquals("John", person.getName());
            assertEquals(30, person.getAge());
        }

        @Test
        void deserialize_ShouldReturnNull_WhenNullJsonAndTypeReferenceProvided() {
            // Arrange
            TypeReference<Person> typeRef = new TypeReference<>() {
            };

            // Act
            Person person = JsonUtils.deserialize(null, typeRef);

            // Assertions
            assertNull(person);
        }

        @Test
        void deserialize_ShouldReturnString_WhenStringTypeReferenceProvided() {
            // Arrange
            String content = "test string";
            TypeReference<String> typeRef = new TypeReference<>() {
            };

            // Act
            String result = JsonUtils.deserialize(content, typeRef);

            // Assertions
            assertEquals("test string", result);
        }

        @Test
        void deserialize_ShouldThrowException_WhenInvalidJsonAndTypeReferenceProvided() {
            // Arrange
            TypeReference<Person> typeRef = new TypeReference<>() {
            };

            // Act
            // Assertions
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.deserialize(INVALID_JSON_WITH_MISSING_QUOTES, typeRef));
            assertNotNull(exception);
        }

        @Test
        void deserialize_ShouldReturnObject_WhenValidJsonNodeProvided() {
            // Arrange
            JsonNode node = JsonUtils.getMapper().readTree(SINGLE_JSON_OBJECT);

            // Act
            Person person = JsonUtils.deserialize(node, Person.class);

            // Assertions
            assertNotNull(person);
            assertEquals("John", person.getName());
            assertEquals(30, person.getAge());
        }

        @Test
        void deserialize_ShouldThrowException_WhenInvalidJsonNodeProvided() {
            // Arrange
            JsonNode node = JsonUtils.getMapper().readTree("{\"name\":\"John\",\"age\":\"invalid\"}");

            // Act
            // Assertions
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.deserialize(node, Person.class));
            assertNotNull(exception);
        }

        @Test
        void deserialize_ShouldReturnObject_WhenValidJsonAndTypeProvided() {
            // Arrange
            Type type = Person.class;

            // Act
            Person person = JsonUtils.deserialize(SINGLE_JSON_OBJECT, type);

            // Assertions
            assertNotNull(person);
            assertEquals("John", person.getName());
            assertEquals(30, person.getAge());
        }

        @Test
        void deserialize_ShouldThrowException_WhenInvalidJsonAndTypeProvided() {
            // Arrange
            Type type = Person.class;

            // Act
            // Assertions
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.deserialize(INVALID_JSON_WITH_MISSING_QUOTES, type));
            assertNotNull(exception);
        }

        @Test
        void deserialize_ShouldReturnJsonNode_WhenValidJsonProvided() {
            // Arrange
            // Act
            JsonNode node = JsonUtils.deserialize(SINGLE_JSON_OBJECT);

            // Assertions
            assertNotNull(node);
            assertTrue(node.has("name"));
            assertTrue(node.has("age"));
            assertEquals("John", node.get("name").asString());
            assertEquals(30, node.get("age").asInt());
        }

        @Test
        void deserialize_ShouldDeserializeList_WhenValidListJsonProvided() {
            // Arrange
            TypeReference<List<Person>> typeRef = new TypeReference<>() {
            };

            // Act
            List<Person> people = JsonUtils.deserialize(VALID_JSON_ARRAY, typeRef);

            // Assertions
            assertNotNull(people);
            assertEquals(2, people.size());
            assertEquals("John", people.getFirst().getName());
            assertEquals(30, people.getFirst().getAge());
        }

        @Test
        void deserialize_ShouldDeserializeMap_WhenValidMapJsonProvided() {
            // Arrange
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
            };

            // Act
            Map<String, Object> result = JsonUtils.deserialize(SINGLE_JSON_OBJECT, typeRef);

            // Assertions
            assertNotNull(result);
            assertEquals("John", result.get("name"));
            assertEquals(30, ((Number) result.get("age")).intValue());
        }

        @Test
        void deserialize_ShouldThrowException_WhenEmptyJsonProvided() {
            // Arrange
            String emptyJson = "{}";

            // Act
            // Assertions
            // Empty object cannot be mapped to Person because age is a primitive int
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.deserialize(emptyJson, Person.class));
            assertNotNull(exception);
        }

        @Test
        void deserialize_ShouldHandleNestedObjects_WhenComplexJsonProvided() {
            // Arrange
            String complexJson = """
                    {
                        "name": "John",
                        "age": 30,
                        "address": {
                            "city": "New York",
                            "country": "USA"
                        }
                    }
                    """;
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
            };

            // Act
            Map<String, Object> result = JsonUtils.deserialize(complexJson, typeRef);

            // Assertions
            assertNotNull(result);
            assertEquals("John", result.get("name"));
            assertInstanceOf(Map.class, result.get("address"));
        }
    }

    @Nested
    @DisplayName("Convert Tests")
    class ConvertTests {

        @Test
        void convert_ShouldReturnConvertedObject_WhenValidObjectProvided() {
            // Arrange
            Person person = new Person("John", 30);

            // Act
            Person result = JsonUtils.convert(person, Person.class);

            // Assertions
            assertNotNull(result);
            assertEquals("John", result.getName());
            assertEquals(30, result.getAge());
        }

        @Test
        void convert_ShouldReturnConvertedObject_WhenValidMapProvided() {
            // Arrange
            Map<String, Object> map = Map.of("name", "Jane", "age", 25);

            // Act
            Person result = JsonUtils.convert(map, Person.class);

            // Assertions
            assertNotNull(result);
            assertEquals("Jane", result.getName());
            assertEquals(25, result.getAge());
        }

        @Test
        void convert_ShouldReturnNull_WhenNullObjectProvided() {
            // Arrange
            // Act
            Person result = JsonUtils.convert(null, Person.class);

            // Assertions
            assertNull(result);
        }

        @Test
        void convert_ShouldConvertList_WhenListProvided() {
            // Arrange
            List<Map<String, Object>> list = List.of(
                    Map.of("name", "John", "age", 30),
                    Map.of("name", "Jane", "age", 25)
            );

            // Act
            @SuppressWarnings("unchecked")
            List<Person> result = JsonUtils.convert(list, List.class);

            // Assertions
            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        void convert_ShouldConvertPrimitiveTypes_WhenPrimitiveProvided() {
            // Arrange
            Integer number = 42;

            // Act
            String result = JsonUtils.convert(number, String.class);

            // Assertions
            assertNotNull(result);
            assertEquals("42", result);
        }

        @Test
        void convert_ShouldThrowException_WhenEmptyMapProvided() {
            // Arrange
            Map<String, Object> emptyMap = Map.of();

            // Act
            // Assertions
            // Empty map cannot be converted to Person because age is a primitive int
            // Jackson 3.x throws exception when trying to map null to primitive types
            assertThrows(MismatchedInputException.class, () -> JsonUtils.convert(emptyMap, Person.class));
        }
    }

    @Nested
    @DisplayName("Convert Value Tests")
    class ConvertValueTests {

        @Test
        void convertValue_ShouldReturnConvertedObject_WhenValidInputProvided() {
            // Arrange
            Person person = new Person("John", 30);
            TypeReference<Person> typeReference = new TypeReference<>() {
            };

            // Act
            Person result = JsonUtils.convertValue(person, typeReference);

            // Assertions
            assertNotNull(result);
            assertEquals("John", result.getName());
            assertEquals(30, result.getAge());
        }

        @Test
        void convertValue_ShouldReturnConvertedObject_WhenValidMapInputProvided() {
            // Arrange
            Map<String, Object> personMap = Map.of("name", "Jane", "age", 25);
            TypeReference<Person> typeReference = new TypeReference<>() {
            };

            // Act
            Person result = JsonUtils.convertValue(personMap, typeReference);

            // Assertions
            assertNotNull(result);
            assertEquals("Jane", result.getName());
            assertEquals(25, result.getAge());
        }

        @Test
        void convertValue_ShouldThrowException_WhenInvalidInputProvided() {
            // Arrange
            String invalidJson = "Invalid JSON string";
            TypeReference<Person> typeReference = new TypeReference<>() {
            };

            // Act
            // Assertions
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.convertValue(invalidJson, typeReference));
            assertEquals("JSON convert value error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void convertValue_ShouldThrowException_WhenMapHasIncorrectDataTypes() {
            // Arrange
            Map<String, Object> malformedMap = Map.of("name", "Alice", "age", "thirty");
            TypeReference<Person> typeReference = new TypeReference<>() {
            };

            // Act
            // Assertions
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.convertValue(malformedMap, typeReference));
            assertEquals("JSON convert value error, please check the logs for more details", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("JSON Reading Tests")
    class JsonReadingTests {

        @Test
        void readJSON_ShouldDeserializeValidJson_WhenUsingClass() {
            // Arrange
            // Act
            Person person = JsonUtils.readJSON(SINGLE_JSON_OBJECT, Person.class);

            // Assertions
            assertNotNull(person);
            assertEquals("John", person.getName());
            assertEquals(30, person.getAge());
        }

        @Test
        void readJSON_ShouldThrowException_WhenJsonIsInvalid() {
            // Arrange
            // Act
            BaseException baseException = assertThrows(BaseException.class, () -> JsonUtils.readJSON(INVALID_JSON_WITH_MISSING_QUOTES, Person.class));

            // Assertions
            assertNotNull(baseException);
            assertEquals("JSON read value error, please check the logs for more details", baseException.getMessage());
        }

        @Test
        void readJSON_ShouldDeserializeValidJson_WhenUsingTypeReference() {
            // Arrange
            // Act
            Map<String, Object> result = JsonUtils.readJSON(SINGLE_JSON_OBJECT, new TypeReference<>() {
            });

            // Assertions
            assertNotNull(result);
            assertEquals("John", result.get("name"));
            assertEquals(30, ((Number) result.get("age")).intValue());
        }

        @Test
        void readJSON_ShouldThrowException_WhenJsonIsInvalidUsingTypeReference() {
            // Arrange
            TypeReference<Object> typeReference = new TypeReference<>() {
            };

            // Act
            BaseException baseException = assertThrows(BaseException.class, () -> JsonUtils.readJSON(INVALID_JSON_WITH_MISSING_QUOTES, typeReference));

            // Assertions
            assertNotNull(baseException);
            assertEquals("JSON read value error, please check the logs for more details", baseException.getMessage());
        }

        @Test
        void readJSON_ShouldReadNullJson_WhenNullStringProvided() {
            // Arrange
            String nullJson = "null";

            // Act
            Person person = JsonUtils.readJSON(nullJson, Person.class);

            // Assertions
            assertNull(person);
        }

        @Test
        void readJSON_ShouldReadEmptyArray_WhenEmptyArrayProvided() {
            // Arrange
            String emptyArray = "[]";

            // Act
            List<Person> people = JsonUtils.readJSON(emptyArray, new TypeReference<>() {
            });

            // Assertions
            assertNotNull(people);
            assertTrue(people.isEmpty());
        }

        @Test
        void readJSON_ShouldThrowException_WhenEmptyObjectProvided() {
            // Arrange
            String emptyObject = "{}";

            // Act
            // Assertions
            // Empty object cannot be mapped to Person because age is a primitive int
            BaseException exception = assertThrows(BaseException.class,
                    () -> JsonUtils.readJSON(emptyObject, Person.class));
            assertNotNull(exception);
            assertEquals("JSON read value error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void readJSON_ShouldReadArrayOfPrimitives_WhenPrimitiveArrayProvided() {
            // Arrange
            String arrayJson = "[1, 2, 3, 4, 5]";

            // Act
            List<Integer> numbers = JsonUtils.readJSON(arrayJson, new TypeReference<>() {
            });

            // Assertions
            assertNotNull(numbers);
            assertEquals(5, numbers.size());
            assertEquals(1, numbers.get(0));
            assertEquals(5, numbers.get(4));
        }
    }

    @Nested
    @DisplayName("Tree Conversion Tests")
    class TreeConversionTests {

        @Test
        void treeToValue_ShouldConvertTree_WhenTreeIsValid() {
            // Arrange
            Map<String, Object> map = Map.of("name", "John", "age", 30);

            // Act
            Person person = JsonUtils.treeToValue(map, Person.class);

            // Assertions
            assertNotNull(person);
            assertEquals("John", person.getName());
            assertEquals(30, person.getAge());
        }

        @Test
        void treeToValue_ShouldReturnNull_WhenInputIsNull() {
            // Arrange
            // Act
            // Assertions
            assertNull(JsonUtils.toObject(null, Person.class));
        }

        @Test
        void treeToValue_ShouldThrowException_WhenInvalidTreeProvided() {
            // Arrange
            Map<String, Object> invalidMap = Map.of("name", "Alice", "age", "invalid");

            // Act
            // Assertions
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.treeToValue(invalidMap, Person.class));
            assertNotNull(exception);
            assertEquals("JSON tree to value error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void toObject_ShouldReturnObject_WhenValidMapProvided() {
            // Arrange
            Map<String, Object> map = Map.of("name", "Jane", "age", 25);

            // Act
            Person person = JsonUtils.toObject(map, Person.class);

            // Assertions
            assertNotNull(person);
            assertEquals("Jane", person.getName());
            assertEquals(25, person.getAge());
        }

        @Test
        void valueToTree_ShouldConvertObject_WhenObjectIsValid() {
            // Arrange
            Person person = new Person("John", 30);

            // Act
            JsonNode node = JsonUtils.valueToTree(person);

            // Assertions
            assertNotNull(node);
            assertTrue(node.has("name"));
            assertTrue(node.has("age"));
            assertEquals("John", node.get("name").asString());
            assertEquals(30, node.get("age").asInt());
        }

        @Test
        void valueToTree_ShouldConvertNull_WhenNullProvided() {
            // Arrange
            // Act
            JsonNode node = JsonUtils.valueToTree(null);

            // Assertions
            assertNotNull(node);
            assertTrue(node.isNull());
        }

        @Test
        void valueToTree_ShouldConvertList_WhenListProvided() {
            // Arrange
            List<Person> people = List.of(new Person("John", 30), new Person("Jane", 25));

            // Act
            JsonNode node = JsonUtils.valueToTree(people);

            // Assertions
            assertNotNull(node);
            assertTrue(node.isArray());
            assertEquals(2, node.size());
        }
    }

    @Nested
    @DisplayName("JSON Writing Tests")
    class JsonWritingTests {

        @Test
        void writeJSON_ShouldSerializeObject_WhenObjectIsValid() {
            // Arrange
            Person person = new Person("John", 30);

            // Act
            String json = JsonUtils.writeJSON(person);

            // Assertions
            assertNotNull(json);
            assertTrue(json.contains("\"name\":\"John\""));
            assertTrue(json.contains("\"age\":30"));
        }

        @Test
        void writeJSON_ShouldReturnEmptyJson_WhenObjectContainsCircularReference() {
            // Arrange
            class CircularReference {
                final CircularReference ref = this;
                final String name = "test";
            }
            CircularReference circularReference = new CircularReference();

            // Act
            String writeJSON = JsonUtils.writeJSON(circularReference);

            // Assertions
            assertNotNull(writeJSON);
            assertEquals("{}", writeJSON);
        }
    }

    @Nested
    @DisplayName("Collection Conversion Tests")
    class CollectionConversionTests {

        @Test
        void convertJSONCollection_ShouldDeserializeValidJsonArray_WhenUsingClass() {
            // Arrange
            // Act
            Collection<Person> persons = JsonUtils.convertJSONCollection(VALID_JSON_ARRAY, Person.class);

            // Assertions
            assertNotNull(persons);
            assertEquals(2, persons.size());
            assertTrue(persons.stream().anyMatch(p -> p.getName().equals("John") && p.getAge() == 30));
            assertTrue(persons.stream().anyMatch(p -> p.getName().equals("Jane") && p.getAge() == 25));
        }

        @Test
        void convertJSONCollection_ShouldReturnCollection_WhenValidJsonArrayProvided() {
            // Arrange
            // Act
            Collection<Person> people = JsonUtils.convertJSONCollection(VALID_JSON_ARRAY, Person.class);

            // Assertions
            assertNotNull(people);
            assertEquals(2, people.size());
            List<Person> personList = (List<Person>) people;
            assertEquals("John", personList.get(0).getName());
            assertEquals(30, personList.get(0).getAge());
            assertEquals("Jane", personList.get(1).getName());
            assertEquals(25, personList.get(1).getAge());
        }

        @Test
        void convertJSONCollection_ShouldThrowException_WhenInvalidJsonArrayProvided() {
            // Arrange
            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.convertJSONCollection(INVALID_JSON_ARRAY, Person.class));

            // Assertions
            assertEquals("JSON convert collection error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void convertJSONCollection_ShouldThrowException_WhenEmptyJsonArrayProvided() {
            // Arrange
            String emptyJsonArray = "[]";

            // Act
            Collection<Person> people = JsonUtils.convertJSONCollection(emptyJsonArray, Person.class);

            // Assertions
            assertNotNull(people);
            assertTrue(people.isEmpty());
        }

        @Test
        void convertJSONCollection_ShouldThrowException_WhenNullJsonProvided() {
            // Arrange
            String nullJson = null;

            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.convertJSONCollection(nullJson, Person.class));

            // Assertions
            assertEquals("JSON convert collection error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void convertJSONCollection_ShouldThrowException_WhenInvalidJsonFormatProvided() {
            // Arrange
            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.convertJSONCollection(INVALID_JSON_ARRAY_FORMAT, Person.class));

            // Assertions
            assertEquals("JSON convert collection error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void readJSONCollection_ShouldReturnCollection_WhenValidJsonArrayProvided() {
            // Arrange
            // Act
            Collection<Person> people = JsonUtils.readJSONCollection(VALID_JSON_ARRAY, Person.class);

            // Assertions
            assertNotNull(people);
            assertEquals(2, people.size());
            List<Person> personList = (List<Person>) people;
            assertEquals("John", personList.get(0).getName());
            assertEquals(30, personList.get(0).getAge());
            assertEquals("Jane", personList.get(1).getName());
            assertEquals(25, personList.get(1).getAge());
        }

        @Test
        void readJSONCollection_ShouldReturnEmptyCollection_WhenEmptyArrayProvided() {
            // Arrange
            String emptyArray = "[]";

            // Act
            Collection<Person> people = JsonUtils.readJSONCollection(emptyArray, Person.class);

            // Assertions
            assertNotNull(people);
            assertTrue(people.isEmpty());
        }

        @Test
        void readJSONCollection_ShouldReadStringCollection_WhenStringArrayProvided() {
            // Arrange
            String stringArray = "[\"hello\", \"world\", \"test\"]";

            // Act
            Collection<String> strings = JsonUtils.readJSONCollection(stringArray, String.class);

            // Assertions
            assertNotNull(strings);
            assertEquals(3, strings.size());
            assertTrue(strings.contains("hello"));
            assertTrue(strings.contains("world"));
            assertTrue(strings.contains("test"));
        }

        @Test
        void readJSONCollection_ShouldThrowException_WhenInvalidArrayProvided() {
            // Arrange
            String invalidArray = "[{\"name\":\"John\",\"age\":\"invalid\"}]";

            // Act
            // Assertions
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.readJSONCollection(invalidArray, Person.class));
            assertNotNull(exception);
            assertEquals("JSON read collection error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void readJSONCollection_ShouldHandleArrayNodeWithObjects_WhenArrayNodeProvided() {
            // Arrange
            ArrayNode arrayNode = JsonUtils.getMapper().createArrayNode();
            arrayNode.add(JsonUtils.getMapper().createObjectNode().put("name", "Alice").put("age", 28));
            arrayNode.add(JsonUtils.getMapper().createObjectNode().put("name", "Bob").put("age", 32));

            // Act
            Collection<Person> people = JsonUtils.readJSONCollection(arrayNode, Person.class);

            // Assertions
            assertNotNull(people);
            assertEquals(2, people.size());
            List<Person> personList = (List<Person>) people;
            assertEquals("Alice", personList.get(0).getName());
            assertEquals(28, personList.get(0).getAge());
            assertEquals("Bob", personList.get(1).getName());
            assertEquals(32, personList.get(1).getAge());
        }

        @Test
        void readJSONCollection_ShouldHandleArrayNodeWithStrings_WhenStringClassProvided() {
            // Arrange
            ArrayNode arrayNode = JsonUtils.getMapper().createArrayNode();
            arrayNode.add("first");
            arrayNode.add("second");
            arrayNode.add("third");

            // Act
            Collection<String> strings = JsonUtils.readJSONCollection(arrayNode, String.class);

            // Assertions
            assertNotNull(strings);
            assertEquals(3, strings.size());
            assertTrue(strings.contains("first"));
            assertTrue(strings.contains("second"));
            assertTrue(strings.contains("third"));
        }

        @Test
        void readJSONCollection_ShouldHandleEmptyArrayNode_WhenEmptyArrayNodeProvided() {
            // Arrange
            ArrayNode emptyArrayNode = JsonUtils.getMapper().createArrayNode();

            // Act
            Collection<Person> people = JsonUtils.readJSONCollection(emptyArrayNode, Person.class);

            // Assertions
            assertNotNull(people);
            assertTrue(people.isEmpty());
        }

        @Test
        void readJSONCollection_ShouldThrowException_WhenInvalidJsonArrayProvided() {
            // Arrange
            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.readJSONCollection(INVALID_JSON_ARRAY, Person.class));

            // Assertions
            assertEquals("JSON read collection error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void readJSONCollection_ShouldThrowException_WhenEmptyJsonArrayProvided() {
            // Arrange
            String emptyJsonArray = "[]";

            // Act
            Collection<Person> people = JsonUtils.readJSONCollection(emptyJsonArray, Person.class);

            // Assertions
            assertNotNull(people);
            assertTrue(people.isEmpty());
        }

        @Test
        void readJSONCollection_ShouldThrowException_WhenNullJsonProvided() {
            // Arrange
            String nullJson = null;

            // Act
            // Assertions
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.readJSONCollection(nullJson, Person.class));
            assertEquals("JSON read collection error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void readJSONCollection_ShouldThrowException_WhenInvalidJsonFormatProvided() {
            // Arrange
            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.readJSONCollection(INVALID_JSON_ARRAY_FORMAT, Person.class));

            // Assertions
            assertEquals("JSON read collection error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void readJSONCollection_ShouldDeserializeValidJsonArray_WhenUsingArrayNode() {
            // Arrange
            ArrayNode arrayNode = JsonUtils.readJSON(STRING_JSON_ARRAY, ArrayNode.class);

            // Act
            Collection<String> result = JsonUtils.readJSONCollection(arrayNode, String.class);

            // Assertions
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains("test1"));
            assertTrue(result.contains("test2"));
        }

        @Test
        void readJSONCollection_ShouldReturnCollection_WhenValidArrayNodeProvided() {
            // Arrange
            ArrayNode jsonArray = (ArrayNode) JsonUtils.getMapper().readTree(VALID_JSON_ARRAY);

            // Act
            Collection<Person> people = JsonUtils.readJSONCollection(jsonArray, Person.class);

            // Assertions
            assertNotNull(people);
            assertEquals(2, people.size());

            List<Person> personList = (List<Person>) people;
            assertEquals("John", personList.get(0).getName());
            assertEquals(30, personList.get(0).getAge());
            assertEquals("Jane", personList.get(1).getName());
            assertEquals(25, personList.get(1).getAge());
        }

        @Test
        void readJSONCollection_ShouldThrowException_WhenInvalidArrayNodeProvided() {
            // Arrange
            ArrayNode invalidJsonArray = (ArrayNode) JsonUtils.getMapper().readTree(INVALID_JSON_ARRAY);

            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.readJSONCollection(invalidJsonArray, Person.class));

            // Assertions
            assertEquals("JSON read collection error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void readJSONCollection_ShouldReturnEmptyCollection_WhenEmptyArrayNodeProvided() {
            // Arrange
            ArrayNode emptyJsonArray = (ArrayNode) JsonUtils.getMapper().readTree("[]");

            // Act
            Collection<Person> people = JsonUtils.readJSONCollection(emptyJsonArray, Person.class);

            // Assertions
            assertNotNull(people);
            assertTrue(people.isEmpty());
        }

        @Test
        void readJSONCollection_ShouldThrowException_WhenNullArrayNodeProvided() {
            // Arrange
            ArrayNode nullArrayNode = null;

            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.readJSONCollection(nullArrayNode, Person.class));

            // Assertions
            assertEquals("JSON read collection error, please check the logs for more details", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("JSON Validation Tests")
    class JsonValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        void isJsonValid_ShouldReturnFalse_WhenJsonIsBlankOrNull(String json) {
            // Arrange
            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(json);

            // Assertions
            assertFalse(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnTrue_WhenJsonArrayIsValid() {
            // Arrange
            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(VALID_JSON_ARRAY);

            // Assertions
            assertTrue(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnTrue_WhenJsonIsValid() {
            // Arrange
            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(SINGLE_JSON_OBJECT);

            // Assertions
            assertTrue(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnFalse_WhenJsonIsMalformed() {
            // Arrange
            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(INVALID_JSON_WITH_MISSING_QUOTES);

            // Assertions
            assertFalse(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnTrue_WhenEmptyObjectProvided() {
            // Arrange
            String emptyObject = "{}";

            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(emptyObject);

            // Assertions
            assertTrue(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnTrue_WhenEmptyArrayProvided() {
            // Arrange
            String emptyArray = "[]";

            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(emptyArray);

            // Assertions
            assertTrue(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnTrue_WhenStringValueProvided() {
            // Arrange
            String stringValue = "\"test string\"";

            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(stringValue);

            // Assertions
            assertTrue(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnTrue_WhenNumberValueProvided() {
            // Arrange
            String numberValue = "123";

            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(numberValue);

            // Assertions
            assertTrue(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnTrue_WhenBooleanValueProvided() {
            // Arrange
            String booleanValue = "true";

            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(booleanValue);

            // Assertions
            assertTrue(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnTrue_WhenNullJsonStringProvided() {
            // Arrange
            String nullValue = "null";

            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(nullValue);

            // Assertions
            assertTrue(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnFalse_WhenUnquotedStringProvided() {
            // Arrange
            String unquotedString = "hello";

            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(unquotedString);

            // Assertions
            assertFalse(isJsonValid);
        }

        @Test
        void isJsonValid_ShouldReturnFalse_WhenTrailingCommaProvided() {
            // Arrange
            String trailingComma = "{\"name\":\"John\",}";

            // Act
            boolean isJsonValid = JsonUtils.isJsonValid(trailingComma);

            // Assertions
            assertFalse(isJsonValid);
        }

        @Test
        void isJsonNullOrValid_ShouldReturnTrue_WhenValidJsonObjectProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonNullOrValid(SINGLE_JSON_OBJECT);

            // Assertions
            assertTrue(result);
        }

        @Test
        void isJsonNullOrValid_ShouldReturnTrue_WhenValidJsonArrayProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonNullOrValid(VALID_JSON_ARRAY);

            // Assertions
            assertTrue(result);
        }

        @Test
        void isJsonNullOrValid_ShouldReturnTrue_WhenEmptyJsonProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonNullOrValid("{}");

            // Assertions
            assertTrue(result);
        }

        @Test
        void isJsonNullOrValid_ShouldReturnFalse_WhenInvalidJsonProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonNullOrValid(INVALID_JSON_WITH_MISSING_QUOTES);

            // Assertions
            assertFalse(result);
        }

        @Test
        void isJsonNullOrValid_ShouldReturnTrue_WhenJsonPrimitivesProvided() {
            // Arrange
            // Act
            // Assertions
            assertTrue(JsonUtils.isJsonNullOrValid("\"string\""));
            assertTrue(JsonUtils.isJsonNullOrValid("123"));
            assertTrue(JsonUtils.isJsonNullOrValid("true"));
            assertTrue(JsonUtils.isJsonNullOrValid("false"));
            assertTrue(JsonUtils.isJsonNullOrValid("null"));
        }

        @Test
        void isJsonNullOrValid_ShouldReturnFalse_WhenMalformedJsonProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonNullOrValid(MALFORMED_JSON_WITH_INVALID_FORMAT);

            // Assertions
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Pretty Print Tests")
    class PrettyPrintTests {

        @Test
        void prettyPrint_ShouldFormatJson_WhenObjectIsValid() {
            // Arrange
            Person person = new Person("John", 30);

            // Act
            String prettyJson = JsonUtils.prettyPrint(person);

            // Assertions
            assertNotNull(prettyJson);
            assertTrue(prettyJson.contains("\"name\""));
            assertTrue(prettyJson.contains("\"John\""));
            assertTrue(prettyJson.contains("\"age\""));
            assertTrue(prettyJson.contains("30"));
        }

        @Test
        void prettyPrint_ShouldReturnEmptyJson_WhenObjectCannotBeSerialized() {
            // Arrange
            Object invalidObject = new Object() {
                @Override
                public String toString() {
                    throw new RuntimeException("Test exception");
                }
            };

            // Act
            String prettyPrint = JsonUtils.prettyPrint(invalidObject);

            // Assertions
            assertNotNull(prettyPrint);
            assertEquals("{}", prettyPrint);
        }

        @Test
        void prettyPrint_ShouldReturnNull_WhenNullProvided() {
            // Arrange
            // Act
            String prettyJson = JsonUtils.prettyPrint(null);

            // Assertions
            assertNotNull(prettyJson);
            assertEquals("null", prettyJson);
        }

        @Test
        void prettyPrint_ShouldFormatList_WhenListProvided() {
            // Arrange
            List<Person> people = List.of(
                    new Person("John", 30),
                    new Person("Jane", 25)
            );

            // Act
            String prettyJson = JsonUtils.prettyPrint(people);

            // Assertions
            assertNotNull(prettyJson);
            assertTrue(prettyJson.contains("John"));
            assertTrue(prettyJson.contains("Jane"));
        }

        @Test
        void prettyPrint_ShouldFormatMap_WhenMapProvided() {
            // Arrange
            Map<String, Object> map = Map.of("name", "Alice", "age", 28);

            // Act
            String prettyJson = JsonUtils.prettyPrint(map);

            // Assertions
            assertNotNull(prettyJson);
            assertTrue(prettyJson.contains("name"));
            assertTrue(prettyJson.contains("Alice"));
        }
    }

    @Nested
    @DisplayName("JSON Object Format Tests")
    class JsonObjectFormatTests {

        @Test
        void isJsonValid_ShouldReturnTrue_WhenValidJsonObjectProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonValid(VALID_JSON_OBJECT);

            // Assertions
            assertTrue(result);
        }

        @Test
        void deserialize_ShouldReturnMap_WhenValidJsonObjectProvided() {
            // Arrange
            // Act
            JsonNode node = JsonUtils.deserialize(VALID_JSON_OBJECT);

            // Assertions
            assertNotNull(node);
            assertTrue(node.isObject());
            assertTrue(node.has("key"));
            assertEquals("value", node.get("key").asString());
        }

        @Test
        void readJSON_ShouldReturnMap_WhenValidJsonObjectProvided() {
            // Arrange
            // Act
            Map<String, Object> result = JsonUtils.readJSON(VALID_JSON_OBJECT, new TypeReference<>() {
            });

            // Assertions
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.containsKey("key"));
            assertEquals("value", result.get("key"));
        }

        @Test
        void isJsonValid_ShouldReturnFalse_WhenInvalidJsonObjectWithTrailingCommaProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonValid(INVALID_JSON_OBJECT);

            // Assertions
            assertFalse(result);
        }

        @Test
        void deserialize_ShouldThrowException_WhenInvalidJsonObjectWithTrailingCommaProvided() {
            // Arrange
            // Act
            BaseException exception = assertThrows(BaseException.class,
                    () -> JsonUtils.deserialize(INVALID_JSON_OBJECT));

            // Assertions
            assertNotNull(exception);
        }

        @Test
        void readJSON_ShouldThrowException_WhenInvalidJsonObjectWithTrailingCommaProvided() {
            // Arrange
            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.readJSON(INVALID_JSON_OBJECT, new TypeReference<Map<String, Object>>() {
            }));

            // Assertions
            assertNotNull(exception);
            assertEquals("JSON read value error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void convertJSONCollection_ShouldThrowException_WhenInvalidJsonObjectWithTrailingCommaProvided() {
            // Arrange
            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.convertJSONCollection(INVALID_JSON_OBJECT, String.class));

            // Assertions
            assertNotNull(exception);
            assertEquals("JSON convert collection error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void isJsonValid_ShouldReturnFalse_WhenInvalidJsonObjectWithTrailingTokensProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonValid(INVALID_JSON_OBJECT_WITH_TRAILING_TOKENS);

            // Assertions
            assertFalse(result);
        }

        @Test
        void deserialize_ShouldThrowException_WhenInvalidJsonObjectWithTrailingTokensProvided() {
            // Arrange
            // Act
            BaseException exception = assertThrows(BaseException.class,
                    () -> JsonUtils.deserialize(INVALID_JSON_OBJECT_WITH_TRAILING_TOKENS));

            // Assertions
            assertNotNull(exception);
        }

        @Test
        void readJSON_ShouldThrowException_WhenInvalidJsonObjectWithTrailingTokensProvided() {
            // Arrange
            // Act
            BaseException exception = assertThrows(BaseException.class, () -> JsonUtils.readJSON(INVALID_JSON_OBJECT_WITH_TRAILING_TOKENS, new TypeReference<Map<String, Object>>() {
            }));

            // Assertions
            assertNotNull(exception);
            assertEquals("JSON read value error, please check the logs for more details", exception.getMessage());
        }

        @Test
        void isJsonNullOrValid_ShouldReturnTrue_WhenValidJsonObjectProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonNullOrValid(VALID_JSON_OBJECT);

            // Assertions
            assertTrue(result);
        }

        @Test
        void isJsonNullOrValid_ShouldReturnFalse_WhenInvalidJsonObjectWithTrailingCommaProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonNullOrValid(INVALID_JSON_OBJECT);

            // Assertions
            assertFalse(result);
        }

        @Test
        void isJsonNullOrValid_ShouldReturnFalse_WhenInvalidJsonObjectWithTrailingTokensProvided() {
            // Arrange
            // Act
            boolean result = JsonUtils.isJsonNullOrValid(INVALID_JSON_OBJECT_WITH_TRAILING_TOKENS);

            // Assertions
            assertFalse(result);
        }

        @Test
        void convertValue_ShouldConvertValidJsonObject_WhenValidJsonObjectProvided() {
            // Arrange
            TypeReference<Map<String, String>> typeRef = new TypeReference<>() {
            };

            // Act
            Map<String, String> result = JsonUtils.convertValue(
                    JsonUtils.getMapper().readTree(VALID_JSON_OBJECT),
                    typeRef
            );

            // Assertions
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("value", result.get("key"));
        }

        @Test
        void serialize_ShouldProduceValidJson_WhenMapProvided() {
            // Arrange
            Map<String, String> map = Map.of("key", "value");

            // Act
            String json = JsonUtils.serialize(map);

            // Assertions
            assertNotNull(json);
            assertTrue(JsonUtils.isJsonValid(json));
            assertTrue(json.contains("\"key\""));
            assertTrue(json.contains("\"value\""));
        }

        @Test
        void readJSON_ShouldReturnMapWithCorrectValues_WhenValidJsonObjectProvided() {
            // Arrange
            // Act
            Map<String, Object> result = JsonUtils.readJSON(VALID_JSON_OBJECT, new TypeReference<>() {
            });

            // Assertions
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.containsKey("key"));
            assertEquals("value", result.get("key"));
        }

        @Test
        void deserialize_ShouldProduceCorrectJsonNode_WhenValidJsonObjectProvided() {
            // Arrange
            // Act
            JsonNode node = JsonUtils.deserialize(VALID_JSON_OBJECT);

            // Assertions
            assertNotNull(node);
            assertTrue(node.isObject());
            assertEquals(1, node.size());
            assertEquals("value", node.get("key").asString());
        }

        @Test
        void treeToValue_ShouldConvertValidJsonObjectToMap_WhenValidJsonObjectNodeProvided() {
            // Arrange
            JsonNode objectNode = JsonUtils.deserialize(VALID_JSON_OBJECT);

            // Act
            Map<String, String> result = JsonUtils.treeToValue(objectNode, Map.class);

            // Assertions
            assertNotNull(result);
            assertTrue(result.containsKey("key"));
            assertEquals("value", result.get("key"));
        }
    }
}
