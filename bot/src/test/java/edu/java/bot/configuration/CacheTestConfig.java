package edu.java.bot.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import java.time.Duration;
import java.util.Objects;

@TestConfiguration
public class CacheTestConfig {

    // @Bean
    // public CacheManager cacheManager() {
    //     CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    //     cacheManager.setCaffeine(Caffeine.newBuilder()
    //         .expireAfterWrite(Duration.ofHours(1))
    //         .maximumSize(100));  // Максимальный размер кэша, можно настроить по необходимости
    //
    //     return cacheManager;
    // }

    // @Bean
    // public CacheManagerCustomizer<ConcurrentMapCacheManager> cacheManagerCustomizer() {
    //     System.out.println("check");
    //     return cacheManager -> cacheManager.getCacheNames()
    //         .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    // }
}
