package com.productapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean
    @Override
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "products", "users", "organizations", "themes", "initiatives", "modules"
        );
        cacheManager.setCaffeine(caffeineCacheBuilder());
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(5000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> 
                    System.out.println("Cache entry removed: " + key + " due to " + cause));
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheResolver cacheResolver() {
        return null;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                System.err.println("Cache get error for key: " + key + " in cache: " + cache.getName());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                System.err.println("Cache put error for key: " + key + " in cache: " + cache.getName());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                System.err.println("Cache evict error for key: " + key + " in cache: " + cache.getName());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                System.err.println("Cache clear error in cache: " + cache.getName());
            }
        };
    }
}