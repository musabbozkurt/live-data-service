package com.mb.livedataservice.data.filter.elastic;

import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Supplier;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarFilter implements Filter {

    private String model;

    private Integer yearOfManufacture;

    private String brand;

    @Override
    public Supplier<Query> toSupplierQuery() {
        val fuzzyQuery = new FuzzyQuery.Builder();

        if (StringUtils.isNotBlank(this.model)) {
            fuzzyQuery.field("model").value(this.model);
        }

        if (Objects.nonNull(this.yearOfManufacture)) {
            fuzzyQuery.field("yearOfManufacture").value(this.yearOfManufacture);
        }

        if (StringUtils.isNotBlank(this.brand)) {
            fuzzyQuery.field("brand").value(this.brand);
        }

        return () -> Query.of(builder -> builder.fuzzy(fuzzyQuery.build()));
    }
}
