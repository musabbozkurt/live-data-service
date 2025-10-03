package com.mb.livedataservice.util;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static co.elastic.clients.elasticsearch._types.query_dsl.Operator.And;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElasticQueryUtils {

    public static Query flexibleTextQuery(String field, String value) {
        return new Query.Builder()
                .bool(b -> b
                        // Highest priority: exact match on keyword field
                        .should(s -> s.term(t -> t.field(field).value(value).boost(100.0f)))
                        // High priority: exact phrase match
                        .should(s -> s.matchPhrase(mp -> mp.field(field).query(value).boost(50.0f)))
                        // Medium-high priority: all words must match in order with phrase prefix
                        .should(s -> s.matchPhrasePrefix(mpp -> mpp.field(field).query(value).boost(40.0f)))
                        // Medium priority: all words must match (AND operator)
                        .should(s -> s.match(m -> m.field(field).query(value).operator(And).boost(30.0f)))
                        // Lower priority: all words must match with minimal fuzziness
                        .should(s -> s.match(m -> m.field(field).query(value).operator(And).fuzziness("1").boost(20.0f)))
                        // Lowest priority: fuzzy match but only for single character differences
                        .should(s -> s.fuzzy(f -> f.field(field).value(value).fuzziness("1").boost(10.0f)))
                        .minimumShouldMatch("1")
                )
                .build();
    }

    public static Query rangeQuery(String field, String firstValue, String lastValue) {
        if (StringUtils.isNotBlank(firstValue) && StringUtils.isBlank(lastValue)) {
            return new TermRangeQuery.Builder()
                    .field(field)
                    .gte(firstValue)
                    .build()
                    ._toRangeQuery()
                    ._toQuery();
        }
        if (StringUtils.isBlank(firstValue) && StringUtils.isNotBlank(lastValue)) {
            return new TermRangeQuery.Builder()
                    .field(field)
                    .gte(lastValue)
                    .build()
                    ._toRangeQuery()
                    ._toQuery();
        }
        return new TermRangeQuery.Builder()
                .field(field)
                .gte(firstValue)
                .lte(lastValue)
                .build()
                ._toRangeQuery()
                ._toQuery();
    }

    public static Query termQuery(String field, String value) {
        return new Query.Builder()
                .term(new TermQuery.Builder()
                        .field(field)
                        .value(value)
                        .build())
                .build();
    }

    public static Query termsQuery(String field, List<String> values) {
        return new Query.Builder()
                .terms(new TermsQuery.Builder()
                        .field(field)
                        .terms(new TermsQueryField.Builder()
                                .value(values.stream()
                                        .map(FieldValue::of)
                                        .toList())
                                .build())
                        .build())
                .build();
    }
}
