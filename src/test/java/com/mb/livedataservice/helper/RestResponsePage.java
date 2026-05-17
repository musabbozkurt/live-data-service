package com.mb.livedataservice.helper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestResponsePage<T> extends PageImpl<T> {

    /**
     * Jackson constructor for deserializing Spring Data Page responses.
     * Uses wrapper types (Integer, Boolean, Long) instead of primitives because
     * Spring Boot's {@code PageSerializationMode.VIA_DTO} may omit fields like
     * {@code first}, {@code last}, {@code number} from the JSON response, resulting in null values.
     * Primitive types cannot accept null and would throw
     * {@code MismatchedInputException: Cannot map null into type int/boolean}.
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestResponsePage(@JsonProperty("content") List<T> content,
                            @JsonProperty("number") Integer number,
                            @JsonProperty("size") Integer size,
                            @JsonProperty("totalElements") Long totalElements,
                            @JsonProperty("pageable") JsonNode pageable,
                            @JsonProperty("first") Boolean first,
                            @JsonProperty("last") Boolean last,
                            @JsonProperty("totalPages") Integer totalPages,
                            @JsonProperty("sort") JsonNode sort,
                            @JsonProperty("numberOfElements") Integer numberOfElements) {
        super(content != null ? content : new ArrayList<>(), PageRequest.of(number != null ? number : 0, size != null && size > 0 ? size : 20), totalElements != null ? totalElements : 0L);
    }

    public RestResponsePage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RestResponsePage(List<T> content) {
        super(content);
    }

    public RestResponsePage() {
        super(new ArrayList<>());
    }
}
