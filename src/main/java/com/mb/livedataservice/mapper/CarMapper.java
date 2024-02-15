package com.mb.livedataservice.mapper;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.mb.livedataservice.api.filter.ApiCarFilter;
import com.mb.livedataservice.api.request.ApiCarRequest;
import com.mb.livedataservice.api.response.ApiCarResponse;
import com.mb.livedataservice.data.filter.elastic.CarFilter;
import com.mb.livedataservice.data.model.elastic.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CarMapper {

    ApiCarResponse map(Car car);

    @Mapping(target = "id", ignore = true)
    Car map(ApiCarRequest apiCarRequest);

    CarFilter map(ApiCarFilter apiCarFilter);

    default List<ApiCarResponse> map(SearchResponse<Car> searchResponse) {
        List<ApiCarResponse> carResponses = new ArrayList<>();
        for (Hit<Car> hit : searchResponse.hits().hits()) {
            carResponses.add(this.map(hit.source()));
        }
        return carResponses;
    }
}
