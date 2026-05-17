package com.mb.livedataservice.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelSpan;
import io.micrometer.tracing.otel.bridge.OtelTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Component;

/**
 * Registers runtime hints for tracing classes so that AOT-compiled applications can
 * properly access tracing context via reflection. This fixes the issue where traceId
 * and spanId are not propagated to MDC in AOT mode.
 */
@Component
@ImportRuntimeHints(TracingRuntimeHints.class)
public class TracingRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register Micrometer Tracing OTel bridge classes for reflection
        hints.reflection().registerType(OtelTracer.class, MemberCategory.values());
        hints.reflection().registerType(OtelCurrentTraceContext.class, MemberCategory.values());
        hints.reflection().registerType(OtelSpan.class, MemberCategory.values());
        hints.reflection().registerType(OtelTraceContext.class, MemberCategory.values());
        hints.reflection().registerType(Tracer.class, MemberCategory.values());
        hints.reflection().registerType(Span.class, MemberCategory.values());
        hints.reflection().registerType(TraceContext.class, MemberCategory.values());

        // Register context propagation related classes
        try {
            Class<?> mdcAccessor = Class.forName("io.micrometer.context.slf4j.Slf4jThreadLocalAccessor");
            hints.reflection().registerType(mdcAccessor, MemberCategory.values());
        } catch (ClassNotFoundException _) {
            // Slf4j accessor may not be on classpath
        }

        try {
            Class<?> observationAccessor = Class.forName("io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor");
            hints.reflection().registerType(observationAccessor, MemberCategory.values());
        } catch (ClassNotFoundException _) {
            // Observation accessor may not be on classpath
        }
    }
}
