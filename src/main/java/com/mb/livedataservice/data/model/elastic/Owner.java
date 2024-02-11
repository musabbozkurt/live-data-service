package com.mb.livedataservice.data.model.elastic;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public record Owner(@Field(type = FieldType.Text)
                    String name,

                    @Field(type = FieldType.Integer)
                    Integer age,

                    @Field(type = FieldType.Boolean, name = "isActive")
                    Boolean isActive) {
}
