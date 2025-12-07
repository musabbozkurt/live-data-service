package com.mb.livedataservice.data.model.elastic;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "car_index")
public record Car(@Id
                  String id,

                  @Field(type = FieldType.Text, name = "model")
                  String model,

                  @Field(type = FieldType.Integer, name = "year")
                  Integer yearOfManufacture,

                  @Field(type = FieldType.Text, name = "brand")
                  String brand,

                  @Field(type = FieldType.Nested, name = "owners")
                  List<Owner> owners) {

}
