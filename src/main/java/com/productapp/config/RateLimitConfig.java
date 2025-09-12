package com.productapp.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    private final ConcurrentHashMap<String, AtomicLong> requestCountsPerIpAddress = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastRequestTimePerIpAddress = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private MeterRegistry meterRegistry;

    public RateLimitConfig() {
        // Cleanup old entries every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanupOldEntries, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }

    public class RateLimitInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
        
        private static final int MAX_REQUESTS_PER_MINUTE = 100;
        private static final int MAX_REQUESTS_PER_HOUR = 1000;
        private static final long MINUTE_MS = 60000; // 1 minute
        private static final long HOUR_MS = 3600000; // 1 hour

        @Override
        public boolean preHandle(HttpServletRequest request, 
                               jakarta.servlet.http.HttpServletResponse response, 
                               Object handler) throws Exception {
            
            String clientIpAddress = getClientIP(request);
            long currentTime = System.currentTimeMillis();
            
            // Check rate limits
            if (isRateLimited(clientIpAddress, currentTime)) {
                // Record rate limit exceeded metric
                Counter.builder("rate_limit.exceeded")
                        .tag("ip", clientIpAddress)
                        .register(meterRegistry)
                        .increment();
                        
                response.setStatus(429);
                response.setHeader("Retry-After", "60");
                response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", String.valueOf((currentTime + MINUTE_MS) / 1000));
                response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\",\"retryAfter\":60}");
                response.setContentType("application/json");
                return false;
            }
            
            // Record successful request
            recordRequest(clientIpAddress, currentTime);
            
            // Add rate limit headers
            AtomicLong currentCount = requestCountsPerIpAddress.get(clientIpAddress);
            long remaining = Math.max(0, MAX_REQUESTS_PER_MINUTE - (currentCount != null ? currentCount.get() : 0));
            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            response.setHeader("X-RateLimit-Reset", String.valueOf((currentTime + MINUTE_MS) / 1000));
            
            return true;
        }
        
        private boolean isRateLimited(String clientIpAddress, long currentTime) {
            AtomicLong requestCount = requestCountsPerIpAddress.computeIfAbsent(clientIpAddress, k -> new AtomicLong(0));
            Long lastRequestTime = lastRequestTimePerIpAddress.get(clientIpAddress);
            
            // Reset counter if more than a minute has passed
            if (lastRequestTime == null || (currentTime - lastRequestTime) > MINUTE_MS) {
                requestCount.set(0);
            }
            
            return requestCount.get() >= MAX_REQUESTS_PER_MINUTE;
        }
        
        private void recordRequest(String clientIpAddress, long currentTime) {
            requestCountsPerIpAddress.computeIfAbsent(clientIpAddress, k -> new AtomicLong(0)).incrementAndGet();
            lastRequestTimePerIpAddress.put(clientIpAddress, currentTime);
        }
        
        private String getClientIP(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            return request.getRemoteAddr();
        }
    }
    
    private void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();
        final long HOUR_MS = 3600000; // 1 hour
        lastRequestTimePerIpAddress.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > HOUR_MS);
        requestCountsPerIpAddress.entrySet().removeIf(entry -> 
            !lastRequestTimePerIpAddress.containsKey(entry.getKey()));
    }
}