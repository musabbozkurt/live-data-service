package com.mb.livedataservice.data.filter.elastic;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.util.function.Supplier;

public interface Filter {

    Supplier<Query> toSupplierQuery();
}
