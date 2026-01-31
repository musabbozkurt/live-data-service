package com.mb.livedataservice.service;

import java.util.Map;

public interface ThymeleafTemplateService {

    /**
     * Processes a Thymeleaf template string with the given context variables.
     *
     * @param templateContent the template content as a string
     * @param variables       the context variables to be used in the template
     * @return the processed template as a string
     */
    String processTemplate(String templateContent, Map<String, Object> variables);
}
