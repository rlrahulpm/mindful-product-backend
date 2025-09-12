package com.productapp.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MetricsConfig {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> {
            String activeProfile = String.join(",", environment.getActiveProfiles());
            registry.config()
                    .commonTags(
                        "application", "mindful-product-backend",
                        "environment", activeProfile.isEmpty() ? "default" : activeProfile,
                        "version", "1.0.0"
                    )
                    .meterFilter(MeterFilter.deny(id -> {
                        String uri = id.getTag("uri");
                        return uri != null && (uri.startsWith("/actuator") || uri.equals("/favicon.ico"));
                    }));
        };
    }

    @Bean
    public Counter apiRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("api.requests.total")
                .description("Total API requests")
                .register(meterRegistry);
    }

    @Bean
    public Timer apiRequestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("api.requests.duration")
                .description("API request duration")
                .register(meterRegistry);
    }

    @Bean
    public Counter cacheHitCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cache.hits")
                .description("Cache hit count")
                .register(meterRegistry);
    }

    @Bean
    public Counter cacheMissCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cache.misses")
                .description("Cache miss count")
                .register(meterRegistry);
    }
}