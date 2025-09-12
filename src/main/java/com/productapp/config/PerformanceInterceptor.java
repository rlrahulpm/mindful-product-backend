package com.productapp.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);
    private static final String REQUEST_START_TIME = "requestStartTime";
    private static final String REQUEST_ID = "requestId";

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute(REQUEST_START_TIME, startTime);
        
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute(REQUEST_ID, requestId);
        MDC.put("requestId", requestId);
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        logger.debug("Starting request {} {} with ID {}", method, uri, requestId);
        
        // Count API requests
        Counter.builder("api.requests.total")
                .tag("method", method)
                .tag("uri", uri)
                .register(meterRegistry)
                .increment();
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) throws Exception {
        try {
            Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
            String requestId = (String) request.getAttribute(REQUEST_ID);
            
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                String method = request.getMethod();
                String uri = request.getRequestURI();
                int status = response.getStatus();
                
                // Record timing
                Timer.builder("api.requests.duration")
                        .tag("method", method)
                        .tag("uri", uri)
                        .tag("status", String.valueOf(status))
                        .register(meterRegistry)
                        .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
                
                // Log slow requests
                if (duration > 1000) {
                    logger.warn("Slow request: {} {} completed in {}ms with status {} (Request ID: {})", 
                               method, uri, duration, status, requestId);
                } else {
                    logger.debug("Request {} {} completed in {}ms with status {} (Request ID: {})", 
                                method, uri, duration, status, requestId);
                }
                
                // Count errors
                if (status >= 400) {
                    Counter.builder("api.requests.errors")
                            .tag("method", method)
                            .tag("uri", uri)
                            .tag("status", String.valueOf(status))
                            .register(meterRegistry)
                            .increment();
                }
            }
        } finally {
            MDC.clear();
        }
    }
}