package com.mb.livedataservice.data.filter;

import com.mb.livedataservice.data.model.QTutorial;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorialFilter implements Filter {

    private String title;

    private String description;

    private boolean published;

    @Override
    public Predicate toPredicate() {
        QTutorial qTutorial = QTutorial.tutorial;
        BooleanExpression predicate = qTutorial.id.isNotNull();

        if (StringUtils.isNotBlank(title)) {
            predicate = predicate.and(qTutorial.title.equalsIgnoreCase(title));
        }

        if (StringUtils.isNotBlank(description)) {
            predicate = predicate.and(qTutorial.description.equalsIgnoreCase(description));
        }

        return predicate;
    }
}
