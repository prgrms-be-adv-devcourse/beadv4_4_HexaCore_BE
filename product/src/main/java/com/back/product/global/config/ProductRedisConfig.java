package com.back.product.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class ProductRedisConfig {
    private final static Duration DEFAULT_TTL = Duration.ofMinutes(10);

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private String port;

    @Bean
    @Primary
    public CacheManager productCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 1. 기본 캐시 설정
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL) // 기본 만료 시간
                .disableCachingNullValues() // null 값은 캐싱 X
                // <K, V> = <String, Json> 형식으로 직렬화
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJacksonJsonRedisSerializer(createJsonMapper())));

        // 2. 캐시 이름별 개별 설정 (만료 시간 차별화)
        Map<String, RedisCacheConfiguration> redisCacheConfigurations = new HashMap<>();
        redisCacheConfigurations.put(ProductCacheNames.BRANDS, defaultCacheConfig.entryTtl(Duration.ofDays(1))); // 브랜드 목록 (1일)
        redisCacheConfigurations.put(ProductCacheNames.CATEGORIES, defaultCacheConfig.entryTtl(Duration.ofDays(1))); // 카테고리 목록 (1일)
        redisCacheConfigurations.put(ProductCacheNames.PRODUCT_DETAIL, defaultCacheConfig.entryTtl(Duration.ofHours(1))); // 상품 상세 (1시간)
        redisCacheConfigurations.put(ProductCacheNames.PRODUCT_SEARCH, defaultCacheConfig.entryTtl(Duration.ofMinutes(10))); // 검색 결과 (10분)
        redisCacheConfigurations.put(ProductCacheNames.PRODUCT_SIMILAR, defaultCacheConfig.entryTtl(Duration.ofMinutes(10))); // 유사 상품 (10분)

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(redisCacheConfigurations)
                .build();
    }

    private JsonMapper createJsonMapper() {
        // 1. 다형성 타입 검증기 설정
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        // 2. 소스코드의 실제 생성자 시그니처에 맞게 초기화
        ProductRecordSupportingTypeResolver typer = new ProductRecordSupportingTypeResolver(typeValidator, DefaultTyping.NON_FINAL);

        // 3. JsonMapper 빌드 시 커스텀 TypeResolver 적용
        return JsonMapper.builder()
                .findAndAddModules() // JSR-310 (JavaTimeModule) 자동 등록
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .disable(FAIL_ON_UNKNOWN_PROPERTIES)
                .setDefaultTyping(typer)
                .build();
    }
}
