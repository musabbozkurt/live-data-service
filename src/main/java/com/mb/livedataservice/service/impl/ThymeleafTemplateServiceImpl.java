package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.service.ThymeleafTemplateService;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

@Service
public class ThymeleafTemplateServiceImpl implements ThymeleafTemplateService {

    private final TemplateEngine springTemplateEngine;

    public ThymeleafTemplateServiceImpl() {
        StringTemplateResolver stringTemplateResolver = new StringTemplateResolver();
        stringTemplateResolver.setTemplateMode(TemplateMode.HTML);

        this.springTemplateEngine = new SpringTemplateEngine();
        this.springTemplateEngine.setTemplateResolver(stringTemplateResolver);
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

        return springTemplateEngine.process(templateContent, context);
    }
}
