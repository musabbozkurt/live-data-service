package com.mb.livedataservice.mapper;

import com.mb.livedataservice.api.request.ApiCarRequest;
import com.mb.livedataservice.api.response.ApiCarResponse;
import com.mb.livedataservice.data.model.elastic.Car;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarMapper {

    ApiCarResponse map(Car car);

    Car map(ApiCarRequest apiCarRequest);
}
