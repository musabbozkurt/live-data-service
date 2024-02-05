package com.mb.livedataservice.data.filter;

import com.querydsl.core.types.Predicate;

public interface Filter {

    Predicate toPredicate();
}
