package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.service.ThymeleafTemplateService;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

@Service
public class ThymeleafTemplateServiceImpl implements ThymeleafTemplateService {

    private final TemplateEngine stringTemplateEngine;

    public ThymeleafTemplateServiceImpl() {
        StringTemplateResolver stringTemplateResolver = new StringTemplateResolver();
        stringTemplateResolver.setTemplateMode(TemplateMode.HTML);

        this.stringTemplateEngine = new TemplateEngine();
        this.stringTemplateEngine.setTemplateResolver(stringTemplateResolver);
    }

    @Override
    public String processTemplate(String templateContent, Map<String, Object> variables) {
        if (templateContent == null) {
            return null;
        }

        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }

        return stringTemplateEngine.process(templateContent, context);
    }
}
