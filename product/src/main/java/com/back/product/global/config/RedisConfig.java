package com.back.product.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {
    private final static Duration DEFAULT_TTL = Duration.ofMinutes(10);

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private String port;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 1. 기본 캐시 설정
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL) // 기본 만료 시간
                .disableCachingNullValues() // null 값은 캐싱 X
                // <K, V> = <String, Json> 형식으로 직렬화
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJacksonJsonRedisSerializer(new JsonMapper())));

        // 2. 캐시 이름별 개별 설정 (만료 시간 차별화)
        Map<String, RedisCacheConfiguration> redisCacheConfigurations = new HashMap<>();
        redisCacheConfigurations.put(CacheNames.EMBEDDING, defaultCacheConfig.entryTtl(Duration.ofDays(7))); // AI 임베딩 7일
        redisCacheConfigurations.put(CacheNames.BRANDS, defaultCacheConfig.entryTtl(Duration.ofDays(1))); // 브랜드 목록 (1일)
        redisCacheConfigurations.put(CacheNames.CATEGORIES, defaultCacheConfig.entryTtl(Duration.ofDays(1))); // 카테고리 목록 (1일)
        redisCacheConfigurations.put(CacheNames.PRODUCT_DETAIL, defaultCacheConfig.entryTtl(Duration.ofHours(1))); // 상품 상세 (1시간)
        redisCacheConfigurations.put(CacheNames.PRODUCT_SEARCH, defaultCacheConfig.entryTtl(Duration.ofMinutes(10))); // 검색 결과 (10분)
        redisCacheConfigurations.put(CacheNames.PRODUCT_SIMILAR, defaultCacheConfig.entryTtl(Duration.ofMinutes(10))); // 유사 상품 (10분)

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(redisCacheConfigurations)
                .build();
    }
}
