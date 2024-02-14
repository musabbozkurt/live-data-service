package com.mb.livedataservice.util;

import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.mb.livedataservice.data.filter.CarFilter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ElasticSearchUtil {

    public static Supplier<Query> createSupplierQuery(CarFilter carFilter) {
        return () -> Query.of(builder -> builder.fuzzy(createFuzzyQuery(carFilter)));
    }

    private static FuzzyQuery createFuzzyQuery(CarFilter carFilter) {
        val fuzzyQuery = new FuzzyQuery.Builder();

        if (StringUtils.isNotBlank(carFilter.getModel())) {
            fuzzyQuery.field("model").value(carFilter.getModel());
        }

        if (Objects.nonNull(carFilter.getYearOfManufacture())) {
            fuzzyQuery.field("yearOfManufacture").value(carFilter.getYearOfManufacture());
        }

        if (StringUtils.isNotBlank(carFilter.getBrand())) {
            fuzzyQuery.field("brand").value(carFilter.getBrand());
        }

        return fuzzyQuery.build();
    }
}
