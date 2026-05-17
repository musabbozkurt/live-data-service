package com.mb.livedataservice.config;

import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;
import io.micrometer.tracing.propagation.Propagator;
import io.opentelemetry.api.trace.Span;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

/**
 * Ensures traceId and spanId are propagated to SLF4J MDC when running with AOT.
 *
 * <p>With AOT (process-aot), the {@code ObservationRegistryPostProcessor} may not properly wire
 * the {@link DefaultTracingObservationHandler} into the {@link ObservationRegistry}. This breaks
 * the chain: Observation scope &rarr; TracingObservationHandler &rarr; OtelTracer &rarr; Slf4JEventListener &rarr; MDC.</p>
 *
 * <p>This configuration:</p>
 * <ol>
 *   <li>Explicitly registers the tracing observation handlers with the ObservationRegistry.</li>
 *   <li>Adds a servlet filter that opens the observation scope and populates MDC from the OTel span.</li>
 *   <li>Adds a HandlerInterceptor as a last resort for MDC propagation inside DispatcherServlet.</li>
 * </ol>
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class TracingMdcAotConfig implements WebMvcConfigurer {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";

    private final Tracer tracer;

    /**
     * Register a HandlerInterceptor to populate MDC inside DispatcherServlet handler execution.
     * This covers the case where the observation scope is opened by the DispatcherServlet itself.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                populateMdcFromCurrentContext();
                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
                MDC.remove(TRACE_ID_KEY);
                MDC.remove(SPAN_ID_KEY);
            }
        }).addPathPatterns("/**");
    }

    /**
     * Explicitly registers the tracing observation handlers with the ObservationRegistry.
     * In AOT mode, the {@code ObservationRegistryPostProcessor} may fail to pick up the
     * tracing handler group; this bean forces registration.
     */
    @Bean
    TracingObservationHandlerRegistrar tracingObservationHandlerRegistrar(ObservationRegistry registry, Tracer tracer, Propagator propagator) {
        return new TracingObservationHandlerRegistrar(registry, tracer, propagator);
    }

    /**
     * A servlet filter (registered AFTER the ServerHttpObservationFilter) that opens the
     * observation scope if needed and reads the current span to populate MDC.
     */
    @Bean
    FilterRegistrationBean<OncePerRequestFilter> tracingMdcFilterRegistration(Tracer micrometerTracer) {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                // Try to populate MDC from Micrometer Tracer first
                var span = micrometerTracer.currentSpan();
                if (span != null) {
                    var ctx = span.context();
                    MDC.put(TRACE_ID_KEY, ctx.traceId());
                    MDC.put(SPAN_ID_KEY, ctx.spanId());
                    try {
                        filterChain.doFilter(request, response);
                    } finally {
                        MDC.remove(TRACE_ID_KEY);
                        MDC.remove(SPAN_ID_KEY);
                    }
                    return;
                }

                // Fallback: read directly from OTel Span.current() (OTel context propagation)
                Span otelSpan = Span.current();
                if (otelSpan.getSpanContext().isValid()) {
                    MDC.put(TRACE_ID_KEY, otelSpan.getSpanContext().getTraceId());
                    MDC.put(SPAN_ID_KEY, otelSpan.getSpanContext().getSpanId());
                    try {
                        filterChain.doFilter(request, response);
                    } finally {
                        MDC.remove(TRACE_ID_KEY);
                        MDC.remove(SPAN_ID_KEY);
                    }
                    return;
                }

                // Last resort: open the observation scope manually if observation exists in request
                Observation observation = (Observation) request.getAttribute("org.springframework.web.filter.ServerHttpObservationFilter.observation");
                if (observation != null) {
                    try (var _ = observation.openScope()) {
                        populateMdcFromCurrentContext();
                        filterChain.doFilter(request, response);
                    } finally {
                        MDC.remove(TRACE_ID_KEY);
                        MDC.remove(SPAN_ID_KEY);
                    }
                    return;
                }

                filterChain.doFilter(request, response);
            }
        });
        // Run after ServerHttpObservationFilter (HIGHEST_PRECEDENCE + 1)
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registration;
    }

    /**
     * Explicitly creates a {@link ContextSnapshotFactory} bean to ensure context propagation
     * works correctly with AOT and virtual threads.
     */
    @Bean
    @ConditionalOnMissingBean
    ContextSnapshotFactory contextSnapshotFactory() {
        return ContextSnapshotFactory.builder().build();
    }

    private void populateMdcFromCurrentContext() {
        // Try Micrometer Tracer
        var span = tracer.currentSpan();
        if (span != null) {
            var ctx = span.context();
            MDC.put(TRACE_ID_KEY, ctx.traceId());
            MDC.put(SPAN_ID_KEY, ctx.spanId());
            return;
        }
        // Fallback to OTel API directly
        Span otelSpan = Span.current();
        if (otelSpan.getSpanContext().isValid()) {
            MDC.put(TRACE_ID_KEY, otelSpan.getSpanContext().getTraceId());
            MDC.put(SPAN_ID_KEY, otelSpan.getSpanContext().getSpanId());
        }
    }

    /**
     * Eagerly registers tracing ObservationHandlers with the registry on startup.
     */
    static class TracingObservationHandlerRegistrar {

        TracingObservationHandlerRegistrar(ObservationRegistry registry, Tracer tracer, Propagator propagator) {
            // Register handlers that bridge Observation -> Tracer -> MDC
            var config = registry.observationConfig();
            config.observationHandler(new DefaultTracingObservationHandler(tracer));
            config.observationHandler(new PropagatingSenderTracingObservationHandler<>(tracer, propagator));
            config.observationHandler(new PropagatingReceiverTracingObservationHandler<>(tracer, propagator));
        }
    }
}
